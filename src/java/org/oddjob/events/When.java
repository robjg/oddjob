/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import java.util.concurrent.Executor;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stoppable;

/**
 * @oddjob.description An Experimental Trigger that fires on evaluating a State 
 * Expression to True.
 * 
 * @oddjob.example
 * 
 * A trigger expression based on the state of some jobs.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TriggerExpressionExample.xml}
 * 
 * 
 * @author Rob Gordon.
 */
public class When<T> extends EventJobBase<T> {
	
	private static final long serialVersionUID = 2018060600L; 
	
	@Override
	protected SwitchoverStrategy getSwitchoverStrategy() {
		return new StopStartSwitchover();
	}

	
	static class StopStartSwitchover implements SwitchoverStrategy {
		
		
		@Override
		public void switchover(Runnable changeValues, Object job, Executor executor) {

			if (job != null) {
				if (job instanceof Stoppable) {

					try {
						((Stoppable) job).stop();
					} catch (FailedToStopException e) {
						throw new RuntimeException(e);
					}
				}
			
				if (job instanceof Resetable) {
					((Resetable) job).hardReset();
				}
			}

			changeValues.run();
			
			if (job != null) {
				if (job != null && job instanceof Runnable) {
					executor.execute((Runnable) job);
				}
			}
		}
	}
	
}
