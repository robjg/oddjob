package org.oddjob.sql;

import org.oddjob.beanbus.BadBeanTransfer;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.sql.SQLJob.OnError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Consumer;

public class BadSQLHandler 
implements Consumer<BadBeanTransfer<String>> {

	private static final Logger logger = LoggerFactory.getLogger(BadSQLHandler.class);
	
	private SQLJob.OnError onError = null;
	
	private BusConductor bus;
	
	@Inject
	public void setBeanBus(BusConductor bus) {
		this.bus = bus;
	}

	@Override
	public void accept(BadBeanTransfer<String> bad) {
		
		String sql = bad.getBadBean();

		Throwable exception = bad.getException().getCause();
		if (exception == null) {
			exception = bad.getException();
		}

		logger.info("Failed executing: " + sql +
				"\n\t" + exception.getClass().getName() + ": " + exception.getMessage());
		
		OnError onError = this.onError;
		if (onError == null) {
			onError = OnError.ABORT;
		}
		
		switch (onError) {
		case CONTINUE:
			break;
		case STOP:
				bus.close();
			break;
		case ABORT:
			logger.error("Aborting...");
			throw bad.getException();
		}
	}

	public SQLJob.OnError getOnError() {
		return onError;
	}

	public void setOnError(SQLJob.OnError onError) {
		this.onError = onError;
	}
	
}
