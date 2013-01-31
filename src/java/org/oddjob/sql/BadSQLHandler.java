package org.oddjob.sql;

import org.apache.log4j.Logger;
import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.BadBeanHandler;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusAware;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.sql.SQLJob.OnError;

public class BadSQLHandler implements BadBeanHandler<String>, BusAware {

	private static final Logger logger = Logger.getLogger(BadSQLHandler.class);
	
	private SQLJob.OnError onError = null;
	
	private BusConductor bus;
	
	@Override
	public void setBeanBus(BusConductor bus) {
		this.bus = bus;
	}
	
	@Override
	public void handle(String sql, BadBeanException e)
			throws BusCrashException {
		
		logger.info("Failed executing: " + sql + 
				"\n\t" + e.getCause().getMessage());
		
		OnError onError = this.onError;
		if (onError == null) {
			onError = OnError.ABORT;
		}
		
		switch (onError) {
		case CONTINUE:
			break;
		case STOP:
			bus.requestBusStop();
			break;
		case ABORT:
			logger.error("Aborting...");
			throw new BusCrashException(e);
		}
	}


	public SQLJob.OnError getOnError() {
		return onError;
	}

	public void setOnError(SQLJob.OnError onError) {
		this.onError = onError;
	}
	
}
