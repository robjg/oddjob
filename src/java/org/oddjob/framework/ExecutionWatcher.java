package org.oddjob.framework;

import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionWatcher {

	private final Runnable action;

	private final AtomicInteger added = new AtomicInteger(); 
	
	private final AtomicInteger executed = new AtomicInteger();
	
	private boolean started;
	
	public ExecutionWatcher(Runnable action) {
		this.action = action;
	}

	public Runnable addJob(final Runnable job) {
		
		added.incrementAndGet();
		
		return new Runnable() {
			
			@Override
			public void run() {
				job.run();
				executed.incrementAndGet();

				boolean perform;
				synchronized (ExecutionWatcher.this) {
					perform = check();
				}
				
				if (perform) {
					action.run();
				}
			}
		};
		
	}

	public void start() {

		boolean perform;
		synchronized (this) {
			started = true;
			perform = check();
		}
		if (perform) {
			action.run();
		}
	}
	
	private boolean check() {
		if (started && added.get() == executed.get()) {
			return true;
		}
		else {
			return false;
		}
	}	
}
