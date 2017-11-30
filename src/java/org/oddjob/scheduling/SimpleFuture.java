/**
 * 
 */
package org.oddjob.scheduling;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// We need to augment concurrent Future to ensure
// complete cancellation. isDone and isCancelled return true even
// if the job is still hogging a thread.
class SimpleFuture {
	private static final Logger logger = LoggerFactory.getLogger(SimpleFuture.class);

	private final RunnableWrapper wrapper;
	
	private final Future<?> future;
	
	SimpleFuture(RunnableWrapper wrapper, Future<?> future) {
		this.wrapper = wrapper;
		this.future = future;
	}
	
	public void cancel() {
		if (wrapper.isRunning()) {
			for (int i = 0; i < 10; ++i) {
				if (!wrapper.interrupt()) {
					logger.info("Cancelled running [" + wrapper + "]."); 
					break;
				}
				logger.info("Cancelling [" + wrapper + "]");
				synchronized (this) {
					try {
						wait(1000);
					} catch (InterruptedException e) {
						logger.warn("Interrupted while cancelling [" + wrapper + 
						"] - It may still be running.");
					}
				}
			}
			if (wrapper.isRunning()) {
				logger.warn("Failed to cancel [" + wrapper + 
				"] after ten attempts.");
			}
		}
		else {
			if (!future.isDone()) {
				future.cancel(true);
				logger.info("Cancelled [" + wrapper + "]."); 
			}
		}
	}
	
	public void waitFor() {
		try {
			future.get();
		} catch (InterruptedException e) {
			logger.info("[" + wrapper + "] Wait interrupted."); 
		} catch (CancellationException e) {
			logger.info("[" + wrapper + "] cancelled."); 
		} catch (ExecutionException e) {
			logger.error("Execution Exception for [" + wrapper + "].", e); 
		}
	}
}
