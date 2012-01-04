package org.oddjob;


abstract public class WaitHelper implements Runnable {

	static final long INTERVAL = 100L;
	
	static final int RETRIES = 50;
	
	private int retries = RETRIES;
	
	private long interval = INTERVAL;

	public abstract boolean condition() throws Exception;
	
	public abstract void onRetry();
	
	@Override
	public final void run() {
		while (true) {
			try {
				if (condition()) {
					break;
				}
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
			
			if (--retries < 0) {
				throw new RuntimeException("Wait helper given up!");
			}
			
			onRetry();
			
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}
}
