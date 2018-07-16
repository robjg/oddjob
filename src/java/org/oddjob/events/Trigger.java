package org.oddjob.events;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

public class Trigger<T> extends EventJobBase<T> {

	private static final long serialVersionUID = 2018071300L; 

	private volatile transient SwitchoverStrategy switchOverStrategy;

	@Override
	protected void onInitialised() {
		switchOverStrategy = new RunOnceSwitchover();
		super.onInitialised();
	}
	
	@Override
	protected SwitchoverStrategy getSwitchoverStrategy() {
		return switchOverStrategy;
	}

	@Override
	protected void onReset() {
		switchOverStrategy = new RunOnceSwitchover();
		super.onReset();
	}
	
	class RunOnceSwitchover implements SwitchoverStrategy {
		
		private final Semaphore ran = new Semaphore(1);
		
		@Override
		public void switchover(Runnable changeValues, Object job, Executor executor) {
			
			if (!ran.tryAcquire()) {
				return;
			}
			
			onStop();
			changeValues.run();
			
			if (job != null) {
				if (job != null && job instanceof Runnable) {
					executor.execute((Runnable) job);
				}
			}
		}
	}
	
}
