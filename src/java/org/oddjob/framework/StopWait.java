package org.oddjob.framework;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.logging.LogEnabled;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

/**
 * A utility class to provide wait until stopped functionality.
 * <p>
 * The default timeout is 5 seconds before a {@link FailedToStopException}
 * is thrown.
 * 
 * @author rob
 *
 */
public class StopWait {

	private final Stateful stateful;
	
	private final Logger logger;
	
	private final long timeout;
	
	public StopWait(Stateful stateful) {
		this(stateful, 5000);
	}
	
	public StopWait(Stateful stateful, long timeout) {
		this.stateful = stateful;
		if (stateful instanceof LogEnabled) {
			logger = Logger.getLogger(((LogEnabled) stateful).loggerName());
		}
		else {
			logger = Logger.getLogger(stateful.getClass());
		}
		this.timeout = timeout;
	}
	
	public void run() throws FailedToStopException {		

		if (new IsStoppable().test(
				stateful.lastJobStateEvent().getJobState())) {
			doWait();
		}
	}
	
	private void doWait() throws FailedToStopException {		
		
		final BlockingQueue<JobState> handoff = new LinkedBlockingQueue<JobState>();
		
		class StopListener implements JobStateListener {
			
			@Override
			public void jobStateChange(JobStateEvent event) {
				handoff.add(event.getJobState());
			}
		};
		
		StopListener listener = new StopListener();
				
		stateful.addJobStateListener(listener);
		
		try {
			while (true) {

				JobState state = handoff.poll(timeout, TimeUnit.MILLISECONDS);
				if (state == null) {
					throw new FailedToStopException(stateful);
				}
				if (!new IsStoppable().test(state)) {
					return;
				}
				logger.debug("[" + stateful + "] is " + 
						state + ", waiting to stop...");
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		finally {
			stateful.removeJobStateListener(listener);
		}
	}	
}
