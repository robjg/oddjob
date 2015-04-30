/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.jobs.job.ResetAction;
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.schedules.Interval;
import org.oddjob.state.CompleteOrNotOp;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateOperator;

/**
 * @oddjob.description 
 * 
 * This is a timer that runs it's job according to the schedule until
 * the schedule expires or the job completes successfully.
 * <p>
 * 
 * @oddjob.example
 * 
 * File Polling. Check every 5 seconds for a file.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/RetryExample.xml}
 * 
 * @oddjob.example
 * 
 * Using Retry with a Timer. A daily job retries twice.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/SimpleTimerWithRetry.xml}
 * 
 * @author Rob Gordon.
 */
public class Retry extends TimerBase {
	
	private static final long serialVersionUID = 2009091400L; 
	
	/**
	 * @oddjob.property
	 * @oddjob.description Used to limit the schedule. Usually this
	 * will be configured to be a parent timer's current interval.
	 * @oddjob.required No.
	 */
	private Interval limits;
	
	@Override
	protected StateOperator getStateOp() {
		return new CompleteOrNotOp();
	}
	
	@Override
	protected void begin() throws ComponentPersistException {

		super.begin();
	
		contextData.clear();

		Date use = getClock().getDate();
		
		// This logic is required because we might be running with a Timer 
		// that is not missing skipped runs.
		if (getLimits() != null && 
				use.compareTo(getLimits().getToDate()) >= 0) {
			use = getLimits().getFromDate();
		}
		
		scheduleFrom(use);
	}
		

	@ArooaAttribute
	public void setLimits(Interval limits) {
		this.limits = limits;
	}

	@Override
	public Interval getLimits() {
		return limits;
	}
	
	@Override
	protected boolean isSkipMissedRuns() {
		return true;
	}
	
	@Override
	protected StateCondition getDefaultHaltOn() {
		return StateConditions.COMPLETE;
	}
	
	@Override
	protected ResetAction getDefaultReset() {
		return ResetActions.SOFT;
	}
}
