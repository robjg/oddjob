/**
 * 
 */
package org.oddjob.scheduling;

class RunnableWrapper implements Runnable {
	
	private final Runnable runnable;
	
	private Thread t;
	
	public RunnableWrapper(Runnable runnable) {
		this.runnable = runnable;
	}

	public void run() {
		synchronized (this) {
			t = Thread.currentThread();
		}
		try {
			runnable.run();
		}
		finally {
			synchronized (this) {
				t = null;
			}
		}
		
	}
	public boolean interrupt() {
		synchronized (this) {
			if (t != null) {
				t.interrupt();
				return true;
			}
			return false;
		}
	}

	public boolean isRunning() {
		synchronized (this) {
			return (t != null);
		}
	}
	
	@Override
	public String toString() {
		return runnable.toString();
	}
}