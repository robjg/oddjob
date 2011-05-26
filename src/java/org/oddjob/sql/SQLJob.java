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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;

import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.BadBeanFilter;
import org.oddjob.beanbus.CrashBusException;
import org.oddjob.beanbus.SimpleBus;


/**
 * @oddjob.description Runs one or more SQL statements.
 * <p>
 * An optional result processor may be provided. Current implementations allow
 * the results to be displayed on a result sheet in a similar style to an
 * SQL query tool, or results can be captured as beans who's properties can
 * be used elsewhere in Oddjob.
 * <p>
 * SQL statements can be parameterised, and can be stored procedure or 
 * function calls. Out parameter values can also be accessed and used
 * elsewhere in Oddjob.
 * 
 * @oddjob.example
 *
 * A simple example show first the execution of multiple statements, 
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
 * in delimiter otherwise the semicolon is inturprited as an end of 
 * statement.
 * 
 * {@oddjob.xml.resource org/oddjob/sql/SQLCallableStatement.xml}
 * 
 */
public class SQLJob
implements Runnable, Serializable, ArooaSessionAware, Stoppable {
	private static final long serialVersionUID = 20051106;
		
//	private static final Logger logger = Logger.getLogger(SqlJob.class);
	
    /**
     * delimiters we support, "NORMAL" and "ROW"
     */
    public enum DelimiterType {
    	
    	NORMAL, ROW,
    }

    /**
     * The action a task should perform on an error,
     * one of "continue", "stop" and "abort"
     */
    public enum OnError {
            CONTINUE,
            STOP,
            ABORT
            ;
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
	 * @oddjob.description Optional result processor.
	 * @oddjob.required No, defaults to false. 
	 */
	private transient SQLResultsProcessor results;
	
	/** The session. */
	private transient ArooaSession session;

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
    	executor = new ParameterisedExecutor();    	
    	parser = new ScriptParser();
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
		
		if (results == null) {
			executor.setResultProcessor(new SQLResultsProcessor() {
				@Override
				public void accept(Object bean) throws BadBeanException, CrashBusException {
				}
			});
		}
		else {
	    	executor.setResultProcessor(results);
		}
		
	    parser.setArooaSession(session);
	    executor.setArooaSession(session);
		
    	BadBeanFilter<String> errorFilter = new BadBeanFilter<String>();
    	errorFilter.setBadBeanHandler(errorHandler);
    	
    	parser.setTo(errorFilter);
    	
    	errorFilter.setTo(executor);
    	
    	SimpleBus<String> bus = new SimpleBus<String>();
    	bus.setDriver(parser);

    	bus.run();
	}

	@Override
	public void stop() {
		parser.stop();
		executor.stop();
	}
		
	public SQLResultsProcessor getResults() {
		return results;
	}

	public void setResults(SQLResultsProcessor results) {
		this.results = results;
	}
	
	////////////////////////////////////////////////////
	// Parser properties
	
	/** 
	 * @oddjob.property input
	 * @oddjob.description The input from where to read the SQL query 
	 * or DML statement(s) to run.
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
     * is property expansion inside inline text enabled?
     *
     * @return true if properties are to be expanded.
     * @since Ant 1.7
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
	 * @oddjob.description 
     * Set the delimiter that separates SQL statements. Defaults to &quot;;&quot;;
     * optional
     *
     * <p>For example, set this to "go" and delimitertype to "ROW" for
     * Sybase ASE or MS SQL Server.</p>
     * 
	 * @oddjob.required No.
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
	 * @oddjob.description 
     * Set the delimiter type: "NORMAL" or "ROW" (default "NORMAL").
     *
     * <p>The delimiter type takes two values - NORMAL and ROW. NORMAL
     * means that any occurrence of the delimiter terminate the SQL
     * command whereas with ROW, only a line containing just the
     * delimiter is recognised as the end of the command.</p>
     * 
	 * @oddjob.required No.
	 * 
     * @param delimiterType the type of delimiter - "normal" or "row".
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
     * @param keepformat The keepformat to set
     */
    public void setKeepFormat(boolean keepformat) {
        this.parser.setKeepFormat(keepformat);
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
	 * @oddjob.description The {@link ConnectionType} to use.
	 * @oddjob.required Yes. 
	 */
	public void setConnection(Connection connection) {
		executor.setConnection(connection);
	}

	public ValueType getParameters(int index) {
		return executor.getParameters(index);
	}

	/** 
	 * @oddjob.property parameters
	 * @oddjob.description Parameters to be bound to statement(s).
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
	 * Is the statement a store procedure.
	 * 
	 * @return
	 */
	public boolean isCallable() {
		return executor.isCallable();
	}

	/** 
	 * @oddjob.property escapeProcessing
	 * @oddjob.description 
     * Set escape processing for statements.
     * 
	 * @oddjob.required No, defaults to false. 
	 * 
     * @param enable if true enable escape processing, default is true.
     */
    public void setEscapeProcessing(boolean enable) {
        executor.setEscapeProcessing(enable);
    }
 
    /**
     * 
     * @return
     */
    public boolean isEscapeProcessing() {
    	return executor.isEscapeProcessing();
    }

    /**
	 * @oddjob.property onError
	 * @oddjob.description 
     * Action to perform when statement fails: continue, stop, or abort
     * optional; default &quot;abort&quot;
     * 
	 * @oddjob.required No, defaults to false. 
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
			return "Sql";
		}
		return name;
	}

}
