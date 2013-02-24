package org.oddjob.sql;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BadBeanTransfer;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.sql.SQLJob.OnError;

public class BadSQLHandler 
extends AbstractDestination<BadBeanTransfer<String>> {

	private static final Logger logger = Logger.getLogger(BadSQLHandler.class);
	
	private SQLJob.OnError onError = null;
	
	private BusConductor bus;
	
	@Inject
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
				bus.requestBusStop();
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
