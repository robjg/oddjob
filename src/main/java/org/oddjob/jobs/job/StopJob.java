package org.oddjob.jobs.job;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.jmx.JMXClientJob;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @oddjob.description A job which stops another job. 
 * <p>
 * Normally The stop job will not complete until the job it is
 * stopping is in a stopped state, however if the
 * stop job is attempting tos stop a parent of itself (and therefore itself) then
 * this stop job will detect this and stop. It will therefore complete
 * even if thing it is trying to stop hasn't fully stopped.
 * 
 * @oddjob.example
 * 
 * Examples elsewhere.
 * <ul>
 *  <li>{@link JMXClientJob} has an example where the stop
 *  job is used to stop a client once the connection is no 
 *  longer needed.</li>
 * </ul>
 * 
 * @author Rob Gordon
 */
public class StopJob extends SerializableJob 
implements Stoppable {
    private static final long serialVersionUID = 20050806;

	/**
	 * Number of seconds to wait for a stop result.
	 */
	public static final int RESULT_POLL_TIMEOUT = 15;

	/** 
	 * @oddjob.property
	 * @oddjob.description Job to stop 
	 * @oddjob.required Yes.
	 */
	private transient Stoppable job;

	/** Used to check we're not stopping ourself. */
	private transient volatile Thread thread;

	private volatile ExecutorService executorService;

	private final BlockingQueue<Result> resultQueue = new LinkedBlockingQueue<>();

	@Inject
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Set the stop node directly.
	 * 
	 * @param node The node to stop.
	 */
	@ArooaAttribute
	public void setJob(Stoppable node) {
		this.job = node;
	}

	/**
	 * Get the node to stop.
	 * 
	 * @return The node.
	 */
	public Stoppable getJob() {
		return this.job;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		
		Stoppable job = Objects.requireNonNull(this.job, "No Job to Stop");

		Executor executorService = Optional.<Executor>ofNullable(this.executorService)
				.orElse(Runnable::run);

		logger().info("Stopping [" + job + "]");

		resultQueue.clear();

		executorService.execute(() -> {
			thread = Thread.currentThread();
			try {
				job.stop();
				this.resultQueue.add(new Result(0));
				// This interrupt check is import as it clears the interrupted flag.
				boolean interrupted = Thread.interrupted();
				logger().debug("Interrupted (we stopped ourself): " + interrupted);
			}
			catch (Exception e) {
				this.resultQueue.add(new Result(e));
			}
			finally {
				thread = null;
			}
		});

		Result result = resultQueue.poll(RESULT_POLL_TIMEOUT, TimeUnit.SECONDS);
		if (result == null) {
			throw new FailedToStopException(job, "failed to to stop within " + RESULT_POLL_TIMEOUT + " seconds");
		}
		if (result.exception != null) {
			throw result.exception;
		}

		return result.result;
	}
	
	@Override
	protected void onStop() throws FailedToStopException {
		
		if (thread == Thread.currentThread()) {
			this.resultQueue.add(new Result(0));
			Thread.currentThread().interrupt();
		}
		else {
			this.resultQueue.add(new Result(1));
		}
	}

	static class Result {

		private final int result;

		private final Exception exception;

		Result(int result) {
			this.result = result;
			this.exception = null;
		}

		Result(Exception exception) {
			this.result = -1;
			this.exception = exception;
		}
	}

}
