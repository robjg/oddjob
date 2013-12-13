package org.oddjob.tools;

/**
 * Helps with waiting for a condition on another thread.
 * @author rob
 *
 */
abstract public class WaitHelper implements Runnable {

	static final long INTERVAL = 100L;
	
	static final int RETRIES = 50;
	
	private final int retries;
	
	private final long interval;

	public WaitHelper() {
		this(INTERVAL, RETRIES);
	}
	
	public WaitHelper(int retries) {
		this(INTERVAL, retries);
	}
	
	public WaitHelper(long interval, int retries) {
		this.interval = interval;
		this.retries = retries;
	}
	
	/**
	 * Subclasses must provide the condition.
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract boolean condition() throws Exception;
	
	/**
	 * Called on retry. Subclasses may override to log a message or 
	 * something.
	 */
	public void onRetry() {}
	
	@Override
	public final void run() {
		int retries = this.retries;
		
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
