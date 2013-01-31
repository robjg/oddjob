package org.oddjob.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusAware;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusException;
import org.oddjob.beanbus.BusListenerAdapter;
import org.oddjob.beanbus.BusCrashException;

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
    
    private DatabaseDialect dialect;
    
    /** The session. */
	private transient ArooaSession session;
	
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public void accept(String sql) throws BadBeanException, BusCrashException {
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
    public void execute(String sql) throws SQLException, ArooaConversionException, BusException, ClassNotFoundException {
		logger.info("Executing: " + sql);
		++executedSQLCount;
		
		if (callable) {
			statement = connection.prepareCall(sql);
		}
		else {
			statement = connection.prepareStatement(sql);			
		}
        statement.setEscapeProcessing(escapeProcessing);

		ParameterHandler parameterHandler = getParameterHandler();
		
		parameterHandler.preExecute();
		
		try {
			statement.execute();
			
			SQLWarning warnings = statement.getWarnings();
			while (warnings != null) {
				logger.warn(warnings.getMessage());
				warnings = warnings.getNextWarning();
			}
						
			parameterHandler.postExecute();
			
			ResultSet results = statement.getResultSet();
			if (results != null) {
				
				PropertyAccessor accessor = session.getTools(
						).getPropertyAccessor().accessorWithConversions(
								session.getTools().getArooaConverter());
				
				ResultSetBeanFactory beanFactory = new ResultSetBeanFactory(
						results, accessor, 
						dialect == null ? 
								new BasicGenericDialect() : dialect);

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
     * Private helper to decide on parameter handling strategy.
     * 
     * @return Handler. Never null.
     * @throws SQLException
     */
    private ParameterHandler getParameterHandler() throws SQLException {
    	
		if (parameters != null && parameters.size() > 0 ) {
			return new ParameterHandlerImpl();
		}
		else {
			return new ParameterHandler() {
				@Override
				public void preExecute() throws SQLException,
						ArooaConversionException {
					// Do Nothing
					
				}
				@Override
				public void postExecute() throws SQLException,
						ArooaConversionException {
					// Do Nothing
				}
			};
		}
		
    }
    
    /**
     * For parameter handling strategy.
     *
     */
    private interface ParameterHandler {
    
    	void preExecute() throws SQLException, ArooaConversionException;
    	
    	void postExecute() throws SQLException, ArooaConversionException;
    }
    
    /**
     * Parameter handling implementation.
     *
     */
    private class ParameterHandlerImpl implements ParameterHandler {
    	
		private final ParameterMetaData paramMetaData;
		
		private final int paramCount;

		private final ArooaConverter converter;
		private final ArooaDescriptor descriptor;

		public ParameterHandlerImpl() throws SQLException {
			this.paramMetaData = statement.getParameterMetaData();
			this.paramCount = paramMetaData.getParameterCount();
			
			this.converter = session.getTools().getArooaConverter();
			this.descriptor = session.getArooaDescriptor();
		}
		
    	@Override
    	public void preExecute() throws SQLException, ArooaConversionException {
			if (parameters.size() < paramCount) {
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
    	}
    	
    	@Override
    	public void postExecute() throws SQLException, ArooaConversionException {
    		
			if (statement instanceof CallableStatement) {
				
				CallableStatement callable = (CallableStatement) statement;
				int paramCount = paramMetaData.getParameterCount();
				
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
	public void setBeanBus(BusConductor bus) {
		bus.addBusListener(new BusListenerAdapter() {
			@Override
			public void busStarting(BusEvent event) throws BusCrashException {
				if (connection == null) {
					throw new BusCrashException("No Connection.");
				}
				try {
					connection.setAutoCommit(autocommit);
					logger.info("Setting autocommit " + autocommit);
				}
				catch (SQLException e) {
					throw new BusCrashException(e);
				}
				successfulSQLCount = 0;
				executedSQLCount = 0;
			}
			@Override
			public void busStopping(BusEvent event) throws BusCrashException {
        		if (!isAutocommit()) {
		        	try {
						connection.commit();
						logger.info("Connection committed.");
					} catch (SQLException e) {
						throw new BusCrashException("Failed to commit.", e);
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
			((BusAware) resultProcessor).setBeanBus(bus);
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

	public DatabaseDialect getDialect() {
		return dialect;
	}

	public void setDialect(DatabaseDialect dialect) {
		this.dialect = dialect;
	}
}
