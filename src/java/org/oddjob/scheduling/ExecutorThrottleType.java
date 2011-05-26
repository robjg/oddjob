package org.oddjob.scheduling;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description Throttle parallel execution. This will limit the
 * number of jobs running in parallel.
 * <p>
 * 
 * @oddjob.example Throttling parallel execution.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/ExecutorThrottleInParallel.xml}
 * 
 * 
 * @author rob
 *
 */
public class ExecutorThrottleType implements ValueFactory<ExecutorService>{

	/**
	 * @oddjob.property
	 * @oddjob.description The maximum number of simultaneous jobs this
	 * throttle will allow.
	 * @oddjob.required Yes.
	 */
	private int limit;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The {@code ExecuutorService} to throttle. This
	 * will be automatically set by Oddjob.
	 * @oddjob.required No.
	 */
	private ExecutorService original;
	
	@Override
	public ExecutorService toValue() throws ArooaConversionException {
		
		if (original == null) {
			throw new ArooaConversionException(
					"No original ExecutorService.");
			
		}
		if (limit < 1) {
			throw new ArooaConversionException(
					"A throttle limit of <1 is not allowed.");
		}
		return new ExecutorServiceThrottle(original, limit);
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int max) {
		this.limit = max;
	}

	public ExecutorService getOriginal() {
		return original;
	}

	@Inject
	public void setOriginal(ExecutorService original) {
		this.original = original;
	}
	
	@Override
	public String toString() {
		return "Throttle: limit=" + limit;
	}
}
