package org.oddjob.state;

import org.oddjob.FailedToStopException;
import org.oddjob.framework.extend.SimultaneousStructural;

/**
 * Base class for Jobs that are designed purely to reflect the state of 
 * their child jobs.
 * 
 * @author rob
 *
 */
abstract public class StateReflector extends SimultaneousStructural {
	private static final long serialVersionUID = 20010082000L;
	
	public void stop() throws FailedToStopException {
		
		if (!childStateReflector.isRunning()) {
			return;
		}
		
		logger().info("Stopping.");		
		
		childHelper.stopChildren();
		
		logger().info("Message sent to stop children.");
	}
	
	@Override
	public boolean isJoin() {
		return true;
	}
}
