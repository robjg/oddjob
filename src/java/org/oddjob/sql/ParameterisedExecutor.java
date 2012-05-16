package org.oddjob.sql;

import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.BusAware;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusException;
import org.oddjob.beanbus.BusListener;
import org.oddjob.beanbus.CrashBusException;

/**
 * Handles the execution of a single SQL statement at a time.
 * 
 * @author rob
 *
 */
public class ParameterisedExecutor 
implements ArooaSessionAware, SQLExecutor, BusAware  {

	private static final Logger logger = Logger.getLogger(SQLJob.class);
	
	/** The connection. */
	private Connection connection;
	
	/** The parameters for parameterised statements or procedures. */
	private transient List<ValueType> parameters;
	
	/** True if the statement is a function or procedure. */
	private boolean callable;
	
	/** The number of statements successfully executed. */
    private int successfulSQLCount = 0;

    /** The number of statements executed. */
    private int executedSQLCount = 0;

    /** The to pass results to. */
	private SQLResultsProcessor resultProcessor;

	/** The statement. */
	private PreparedStatement statement;
	
    /** Argument to Statement.setEscapeProcessing */
    private boolean escapeProcessing = true;

    /** Autocommit flag. Default value is false */
    private boolean autocommit = false;
    
    /** The session. */
	private transient ArooaSession session;

	
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public void accept(String sql) throws BadBeanException {
    	try {
    		execute(sql);
    	} 
    	catch (BadBeanException e) {
    		throw e;
    	}
    	catch (Exception e) {
    		throw new BadBeanException(sql, e);
    	}
    }
    
    /**
     * Execute the SQL statement.
     * 
     * @param sql the SQL statement to execute
     * 
     * @throws SQLException on SQL problems
     * @throws ConversionFailedException 
     * @throws NoConversionAvailableException 
     * @throws BusException 
     * @throws ClassNotFoundException 
     */
    public void execute(String sql) throws SQLException, NoConversionAvailableException, ConversionFailedException, BusException, ClassNotFoundException {
		logger.info("Executing: " + sql);
		++executedSQLCount;
		
		if (callable) {
			statement = connection.prepareCall(sql);
		}
		else {
			statement = connection.prepareStatement(sql);			
		}
        statement.setEscapeProcessing(escapeProcessing);

		ParameterMetaData paramMetaData = statement.getParameterMetaData();
		ArooaConverter converter = session.getTools().getArooaConverter();
		ArooaDescriptor descriptor = session.getArooaDescriptor();
		
		int paramCount = paramMetaData.getParameterCount();
		if ((parameters == null ? 0 : parameters.size()) < paramCount) {
			throw new IllegalStateException("Parameters expected " + paramCount);
		}
		
		for (int i = 1; i <= paramCount; ++i) {
			int mode = paramMetaData.getParameterMode(i);
			if (mode == ParameterMetaData.parameterModeIn
					|| mode == ParameterMetaData.parameterModeInOut) {
				
				ArooaValue value = parameters.get(i - 1).getValue();
				String className = paramMetaData.getParameterClassName(i);				
				Class<?> required = descriptor.getClassResolver().findClass(className);
				Object converted = converter.convert(value, required); 
				
				logger.info("Setting parameter " + i + " to [" + converted + "]");
				if (converted == null) {
					statement.setNull(i, paramMetaData.getParameterType(i));
				}
				else {
					statement.setObject(i, converted);
				}
			}
			else {
				logger.info("Registering parameter " + i + " as an Out Parameter");
				((CallableStatement) statement).registerOutParameter(
						i, paramMetaData.getParameterType(i));
			}
		}

		try {
			statement.execute();
			
			SQLWarning warnings = statement.getWarnings();
			while (warnings != null) {
				logger.warn(warnings.getMessage());
				warnings = warnings.getNextWarning();
			}
						
			if (statement instanceof CallableStatement) {
				
				CallableStatement callable = (CallableStatement) statement;
				
				for (int i = 1; i <= paramCount; ++i) {
					int mode = paramMetaData.getParameterMode(i);
					if (mode == ParameterMetaData.parameterModeOut
							|| mode == ParameterMetaData.parameterModeInOut) {
						Object out = callable.getObject(i);

						logger.info("Setting parameter " + i + " to [" + out + "]");										
						ArooaValue value = converter.convert(out, ArooaValue.class);
				
						parameters.get(i - 1).setValue(value);
					}
				}
			}
			
			ResultSet results = statement.getResultSet();
			if (results != null) {
				
				PropertyAccessor accessor = session.getTools(
						).getPropertyAccessor().accessorWithConversions(
								session.getTools().getArooaConverter());
				
				ResultSetBeanFactory beanFactory = new ResultSetBeanFactory(
						results, 
						accessor, 
						getClass().getClassLoader());

				List<?> rows= beanFactory.all();

				logger.info("" + rows.size() + " row(s) returned.");
				resultProcessor.accept(rows);
			}
			else {
				int updateCount = statement.getUpdateCount();
				logger.info("" + updateCount + " row(s) affected.");
				resultProcessor.accept(new UpdateCount(updateCount));
			}
			
			++successfulSQLCount;
		}
		finally {		
			statement.close();
			statement = null;
		}
	}

    /**
     * Cancel the statement. Used by {@link SQLJob#stop()}. 
     */
    public void stop() {
    	Statement stmt = this.statement;
    	if (stmt != null) {
    		try {
				stmt.cancel();
			} catch (SQLException e) {
				logger.debug("Failed to cancel.", e);
			}
    	}
    }
    
	@Override
	public void setBus(BeanBus bus) {
		bus.addBusListener(new BusListener() {
			@Override
			public void busStarting(BusEvent event) throws CrashBusException {
				if (connection == null) {
					throw new CrashBusException("No Connection.");
				}
				try {
					connection.setAutoCommit(autocommit);
					logger.info("Setting autocommit " + autocommit);
				}
				catch (SQLException e) {
					throw new CrashBusException(e);
				}
				successfulSQLCount = 0;
				executedSQLCount = 0;
			}
			@Override
			public void busStopping(BusEvent event) throws CrashBusException {
        		if (!isAutocommit()) {
		        	try {
						connection.commit();
						logger.info("Connection committed.");
					} catch (SQLException e) {
						throw new CrashBusException("Failed to commit.", e);
					}
        		}
			}
			@Override
			public void busCrashed(BusEvent event, BusException e) {
				if (connection != null && !isAutocommit()) {
		        	try {
						connection.rollback();
						logger.info("Connection rolled back.");
					} catch (SQLException e1) {
						logger.error("Failed to rollback.", e1);
					}
				}
			}
			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
				
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						logger.error("Failed closing connection.", e);
					}
				}
				
		        logger.info(successfulSQLCount + " of " + executedSQLCount + " SQL statements executed successfully");   	
		        
			}
		});
		if (resultProcessor instanceof BusAware) {
			((BusAware) resultProcessor).setBus(bus);
		}
	}
	
	/**
	 * Set the result processor.
	 * 
	 * @param processor The result processor to pass results to.
	 */
	public void setResultProcessor(SQLResultsProcessor processor) {
		this.resultProcessor = processor;
	}

	/**
	 * Set the connection to use. This will be closed when the bus
	 * is stopped.
	 * 
	 * @param connection The connection.
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
    /**
     * Auto commit flag for database connection;
     * optional, default false.
     * 
     * @param autocommit The autocommit to set
     */
    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    /**
     * Getter for autocommit.
     * 
     * @return autocommit flag.
     */
    public boolean isAutocommit() {
		return autocommit;
	}
    
    /**
     * Getter for escapeProcessing.
     * 
     * @return escapeProcessing flag.
     */
	public boolean isEscapeProcessing() {
		return escapeProcessing;
	}

	/**
	 * Setter for escapeProcessing.
	 * 
	 * @param escapeProcessing escapeProcessing flag.
	 */
	public void setEscapeProcessing(boolean escapeProcessing) {
		this.escapeProcessing = escapeProcessing;
	}

	/**
	 * Get parameter by index.
	 * 
	 * @param index The index.
	 * 
	 * @return The parameter or null.
	 * 
	 * @throws IndexOutOfBoundsException
	 */
	public ValueType getParameters(int index) 
	throws IndexOutOfBoundsException {
		if (parameters == null) {
			return null;
		}
		else {
			return parameters.get(index);
		}
	}

	/**
	 * Set parameter by index.
	 * 
	 * @param index The index.
	 * 
	 * @param parameter The parameter. Null to remove.
	 * 
	 * @throws IndexOutOfBoundsException
	 */
	public void setParameters(int index, ValueType parameter) 
	throws IndexOutOfBoundsException {
		if (parameters == null) {
			parameters = new ArrayList<ValueType>();
		}
		if (parameter == null) {
			this.parameters.remove(index);
		}
		else {
			this.parameters.add(index, parameter);	
		}
	}

	/**
	 * Getter for callable statement flag.
	 * 
	 * @return The callable flag.
	 */
	public boolean isCallable() {
		return callable;
	}

	/**
	 * Setter for callable statement flag.
	 * 
	 * @param callable The callable flag.
	 */
	public void setCallable(boolean callable) {
		this.callable = callable;
	}    
	
	/**
	 * Getter for executedSQLCount.
	 * 
	 * @return The number of SQL statements executed.
	 */
	public int getExecutedSQLCount() {
		return executedSQLCount;
	}
	
	/**
	 * Getter for successful SQL count.
	 * 
	 * @return The number of SQL statements successfully executed.
	 */
	public int getSuccessfulSQLCount() {
		return successfulSQLCount;
	}
}
