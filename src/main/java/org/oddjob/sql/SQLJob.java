/*
 * Copyright (c) 2005, Rob Gordon (Apart from the Ant bits).
 *
 * This source code is heavily based on source code from the Apache
 * Ant project. As such the following is included:
 * ------------------------------------------------------------------
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.oddjob.sql;

import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.IdentifiableValueType;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.beanbus.Destination;
import org.oddjob.beanbus.SimpleBusConductor;
import org.oddjob.beanbus.destinations.BadBeanFilter;
import org.oddjob.io.BufferType;
import org.oddjob.io.FileType;

import java.io.*;
import java.sql.Connection;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @oddjob.description Runs one or more SQL statements.
 * <p>
 * <h3>Parsing</h3>
 * The SQL will be parsed and broken into individual statements
 * before being executed using JDBC. The statements are broken according
 * to the <code>delimiter</code> and <code>delimiterType</code> properties. 
 * Setting the <code>expandProperties</code> property to true will cause
 * Oddjob to expand ${} expressions within the SQL. Comments are achieved
 * by starting a line with <code>--</code> or <code>//</code> or 
 * <code>REM</code>. Note that <code>/* &#42;/</code> is not yet supported.
 * 
 * <h3>Result Processing</h3>
 * An optional result processor may be provided. {@link SQLResultsSheet} 
 * allows the results to be displayed on a result sheet in a similar style 
 * to an SQL query tool. {@link SQLResultsBean} allows results to be 
 * captured as beans who's properties can be used elsewhere in Oddjob.
 * 
 * <h3>Errors and Auto Commit</h3>
 * The <code>onError</code> property controls what to do if a statement fails.
 * By default it is ABORT. Auto commit is false by default so the changes
 * are rolled back. If auto commit is true the ABORT has the same affect as
 * STOP which commits statements already executed.
 * 
 * <h3>Parameterised Statements and Procedures</h3>
 * SQL statements can be parameterised, and can be stored procedure or 
 * function calls. Out parameter values can also be accessed and used
 * elsewhere in Oddjob by wrapping them with an 
 * {@link IdentifiableValueType}. See example 2 for an example of this.
 * 
 * <h3>Caveats</h3>
 * SQLServer stored procedures with parameters must be made using the JDBC
 * style call. E.g. { call sp_help(?) } otherwise an exception is thrown
 * from <code>getParameterMetaData</code>.
 * 
 * 
 * @oddjob.example
 *
 * A simple example shows first the execution of multiple statements, 
 * then a simple parameterised query.
 *
 * {@oddjob.xml.resource org/oddjob/sql/SQLFirstExample.xml}
 *
 * The results are made available to the echo jobs using a 
 * {@link SQLResultsBean}.
 *
 * @oddjob.example
 * 
 * An Callable Statement example. Showing support for IN, INOUT, and OUT
 * parameters. Note that declaring the stored procedure requires a change
 * in delimiter otherwise the semicolon is interrupted as an end of
 * statement.
 * 
 * {@oddjob.xml.resource org/oddjob/sql/SQLCallableStatement.xml}
 * 
 * @author rob and Ant.
 */
public class SQLJob
implements Runnable, Serializable, ArooaSessionAware, Stoppable {

	private static final long serialVersionUID = 20051106;
		
//	private static final Logger logger = LoggerFactory.getLogger(SqlJob.class);
	
    /**
     * delimiter type between SQL statements.
     */
    public enum DelimiterType {
    	NORMAL, 
    	ROW,
    }

    /**
     * The action a task should perform on an error.
     */
    public enum OnError {
    	CONTINUE,
    	STOP,
    	ABORT
	}
 
    /** Parses the SQL. */
    private transient ScriptParser parser;
	
    /** Handles errors. */
    private transient BadSQLHandler errorHandler;
    
    /** Executes the SQL. */
    private transient ParameterisedExecutor executor;
        
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;

	/** 
	 * @oddjob.property
	 * @oddjob.description Optional result processor. Probably one of
	 * {@link SQLResultsBean} or {@link SQLResultsSheet}.
	 * @oddjob.required No, defaults to none. 
	 */
	private transient Consumer<Object> results;
	
	/** The session. */
	private transient ArooaSession session;

	private transient SimpleBusConductor conductor;

	/**
	 * Constructor.
	 */
	public SQLJob() {
		completeConstruction();
	}

	/**
	 * For serialisation.
	 */
    private void completeConstruction() {
    	parser = new ScriptParser();

    	executor = new ParameterisedExecutor();    	

    	errorHandler = new BadSQLHandler();     	
    }
	
	@Override
	@ArooaHidden
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		SQLResultHandler resultHandler =
				Optional.ofNullable(results)
				.<SQLResultHandler>map(r -> new SQLResultsBus(r, session))
				.orElseGet(DefaultResultsHandler::new);

		executor.setResultProcessor(resultHandler);
		
	    parser.setArooaSession(session);
	    
	    executor.setArooaSession(session);

    	BadBeanFilter<String> errorFilter = new BadBeanFilter<>();
    	errorFilter.setBadBeanHandler(errorHandler);

		conductor = new SimpleBusConductor(parser, errorFilter, executor, resultHandler);

		executor.setBeanBus(conductor);

		parser.setTo(sql -> {
    		try {
    			errorFilter.accept(sql);
			}
    		catch (RuntimeException e) {
				conductor.actOnBusCrash(e);
				throw e;
			}
		});
    	
    	errorFilter.setTo(executor);

		errorHandler.setBeanBus(conductor);

    	conductor.run();
    	conductor.close();
	}


	@Override
	public void stop() {
		conductor.close();
	}

	/**
	 * Getter for results.
	 * 
	 * @return Result Handler. May be null.
	 */
	public Consumer<Object> getResults() {
		return results;
	}

	/**
	 * Setter for results.
	 * 
	 * @param results Result Handler. May be null.
	 */
	@Destination
	public void setResults(Consumer<Object> results) {
		this.results = results;
	}
	
	////////////////////////////////////////////////////
	// Parser properties
	
	/** 
	 * @oddjob.property input
	 * @oddjob.description The input from where to read the SQL query 
	 * or DML statement(s) to run. Probably either {@link FileType} for
	 * reading the SQL from a file or {@link BufferType} for configuring
	 * the SQL in line. 
	 * @oddjob.required Yes. 
	 */
	public void setInput(InputStream sql) {
		parser.setInput(sql);
	}
	
	/** 
	 * @oddjob.property expandProperties
	 * @oddjob.description Enable property expansion inside the SQL statements
	 * read from the input.
	 * @oddjob.required No, defaults to false. 
	 */
    public void setExpandProperties(boolean expandProperties) {
        this.parser.setExpandProperties(expandProperties);
    }

    /**
     * Is property expansion inside inline text enabled?
     *
     * @return true if properties are to be expanded.
     */
    public boolean getExpandProperties() {
        return this.parser.isExpandProperties();
    }

	/** 
	 * @oddjob.property encoding
	 * @oddjob.description Set the string encoding to use on the SQL read in.
	 * @oddjob.required No. 
	 */
    public void setEncoding(String encoding) {
        this.parser.setEncoding(encoding);
    }

    /**
     * Get the input encoding name.
     * 
     * @return
     */
    public String getEncoding() {
    	return this.parser.getEncoding();
    }
    
	/** 
	 * @oddjob.property delimiter
	 * @oddjob.description Set the delimiter that separates SQL statements. 
	 * Defaults to a semicolon.
     * <p>
     * For scripts that use a separate line delimiter like "GO" 
     * also set the <code>delimiterType</code> to "ROW".
     * <p>
     * The delimiter is case insensitive so either "GO" or "go" can be 
     * used interchangeably.
     * 
	 * @oddjob.required No. Defaults to ;
	 *  
     * @param delimiter the separator.
     */
    public void setDelimiter(String delimiter) {
        this.parser.setDelimiter(delimiter);
    }

    /**
     * Get the statement delimiter.
     * 
     * @return
     */
    public String getDelimiter() {
    	return this.parser.getDelimiter();
    }
    
    /**
	 * @oddjob.property delimiterType
	 * @oddjob.description Set the delimiter type: NORMAL or ROW.
     * <p>
     * NORMAL means that any occurrence of the delimiter terminates the SQL
     * command whereas with ROW, only a line containing just the
     * delimiter is recognised as the end of the command.
     * <p>
     * ROW is used with delimiters such as GO.
     * 
	 * @oddjob.required No, defaults to NORMAL.
	 * 
     * @param delimiterType the type of delimiter - "NORMAL" or "ROW".
     */
    public void setDelimiterType(DelimiterType delimiterType) {
        this.parser.setDelimiterType(delimiterType);
    }

    /**
     * Get the delimiter type.
     * 
     * @return
     */
    public DelimiterType getDelimiterType() {
    	return this.parser.getDelimiterType();
    }
    
	/**
	 * @oddjob.property keepFormat
	 * @oddjob.description Whether or not the format of the
	 * SQL should be preserved.
	 * @oddjob.required No. Defaults to false. 
     *
     * @param keepFormat The true or false to keep the format of the SQL.
     */
    public void setKeepFormat(boolean keepFormat) {
        this.parser.setKeepFormat(keepFormat);
    }
    
    /**
     * Get if SQL keeps input format.
     * 
     * @return
     */
    public boolean isKeepFormat() {
    	return this.parser.isKeepFormat();
    }

	////////////////////////////////////////////////////
	// Executor

	/**
	 * @oddjob.property connection
	 * @oddjob.description The connection to use. This can be provided
	 * by a {@link ConnectionType} or by some other means such as custom
	 * data source. This SQL job will always close the connection once
	 * it has run.
	 * @oddjob.required Yes. 
	 */
	public void setConnection(Connection connection) {
		executor.setConnection(connection);
	}

	/**
	 * @oddjob.property autocommit
	 * @oddjob.description Autocommit statements once executed.
	 * @oddjob.required No, defaults to false.
	 * 
	 * @param autocommit
	 */
	public void setAutocommit(boolean autocommit) {
		executor.setAutocommit(autocommit);
	}
	
	/**
	 * Getter for autocommit.
	 * 
	 * @return
	 */
	public boolean isAutocommit() {
		return executor.isAutocommit();
	}
	
	/**
	 * Indexed getter for parameter types.
	 * 
	 * @param index
	 * @return
	 */
	public ValueType getParameters(int index) {
		return executor.getParameters(index);
	}

	/** 
	 * @oddjob.property parameters
	 * @oddjob.description Parameters to be bound to statement(s). This
	 * is either a {@link ValueType} or an {@link IdentifiableValueType} 
	 * if the parameter is an out parameter that is to be identifiable by 
	 * an id for other jobs to access.
	 * @oddjob.required No. 
	 */
	public void setParameters(int index, ValueType parameter) {
		executor.setParameters(index, parameter);
	}

	/** 
	 * @oddjob.property callable
	 * @oddjob.description If the statement calls a stored procedure.
	 * @oddjob.required No, defaults to false. 
	 */
	public void setCallable(boolean callable) {
		executor.setCallable(callable);
	}

	/**
	 * Is the statement a stored procedure.
	 * 
	 * @return
	 */
	public boolean isCallable() {
		return executor.isCallable();
	}

	/** 
	 * @oddjob.property escapeProcessing
	 * @oddjob.description 
     * Set escape processing for statements. See the java doc for 
     * <code>Statement.setEscapeProcessing</code> for more information.
     * 
	 * @oddjob.required No, defaults to false. 
	 * 
     * @param enable if true enable escape processing, default is true.
     */
    public void setEscapeProcessing(boolean enable) {
        executor.setEscapeProcessing(enable);
    }
 
    /**
     * Getter for escapeProcessing.
     * 
     * @return
     */
    public boolean isEscapeProcessing() {
    	return executor.isEscapeProcessing();
    }

	/** 
	 * @oddjob.property dialect
	 * @oddjob.description Allows a {@link DatabaseDialect} to be provided 
	 * that can tune the way the result set is processed.
     * 
	 * @oddjob.required No. A default is used.
	 * 
     * @param dialect The Database Dialect.
     */
    public void setDialect(DatabaseDialect dialect) {
        executor.setDialect(dialect);
    }
 
    /**
     * Getter for dialect.
     * 
     * @return
     */
    public DatabaseDialect getDialect() {
    	return executor.getDialect();
    }    
    
    /**
	 * @oddjob.property onError
	 * @oddjob.description What to do when a statement fails:
	 * <dl>
	 * <dt>CONTINUE</dt>
	 * <dd>Ignore the failure and continue executing.</dd>
	 * <dt>STOP</dt>
	 * <dd>Commit what has been executed but don't execute any more.</dd>
	 * <dt>ABORT</dt>
	 * <dd>Rollback what has been executed and don't execute any more.</dd>
	 * </dl>
     * Note that if <code>autocommit</code> is true then ABORT behaves
     * like STOP as no roll back is possible.
     * 
	 * @oddjob.required No, defaults to ABORT. 
	 * 
     * @param action the action to perform on statement failure.
     */
    public void setOnError(OnError action) {
        this.errorHandler.setOnError(action);
    }

    /**
     * Get on error action.
     * 
     * @return
     */
    public OnError getOnError() {
    	return this.errorHandler.getOnError();
    }
    
    /**
     * @oddjob.property executedSQLCount
     * @oddjob.description The number of SQL statements executed.
     * 
     * @return The number.
     */
    public int getExecutedSQLCount() {
    	return this.executor.getExecutedSQLCount();   	
    }
    
    /**
     * @oddjob.property successfulSQLCount
     * @oddjob.description The number of SQL statements successfully executed.
     * 
     * @return The number.
     */
    public int getSuccessfulSQLCount() {
    	return this.executor.getSuccessfulSQLCount();   	
    }
        
	////////////////////////////////////////////////////
	// Serialisation
	
	/**
	 * Custom serialsation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}

}
