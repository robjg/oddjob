package org.oddjob.sql;

import org.apache.log4j.Logger;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BadBeanTransfer;
import org.oddjob.beanbus.BusAware;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.sql.SQLJob.OnError;

public class BadSQLHandler 
extends AbstractDestination<BadBeanTransfer<String>>
implements BusAware {

	private static final Logger logger = Logger.getLogger(BadSQLHandler.class);
	
	private SQLJob.OnError onError = null;
	
	private BusConductor bus;
	
	@Override
	public void setBeanBus(BusConductor bus) {
		this.bus = bus;
	}
	
	@Override
	public boolean add(BadBeanTransfer<String> bad) {
		
		String sql = bad.getBadBean();
		
		logger.info("Failed executing: " + sql + 
				"\n\t" + bad.getException().getCause().getMessage());
		
		OnError onError = this.onError;
		if (onError == null) {
			onError = OnError.ABORT;
		}
		
		switch (onError) {
		case CONTINUE:
			break;
		case STOP:
			try {
				bus.requestBusStop();
			} catch (BusCrashException e) {
				throw new RuntimeException(e);
			}
			break;
		case ABORT:
			logger.error("Aborting...");
			throw bad.getException();
		}
		
		return true;
	}


	public SQLJob.OnError getOnError() {
		return onError;
	}

	public void setOnError(SQLJob.OnError onError) {
		this.onError = onError;
	}
	
}
