package org.oddjob.examples;

import org.oddjob.FailedToStopException;
import org.oddjob.framework.Service;

public class NonCachingPriceService implements Service {

	@Override
	public void start() throws Exception {
	}
	
	@Override
	public void stop() throws FailedToStopException {
	}
}
