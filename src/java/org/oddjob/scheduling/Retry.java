/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;

import org.oddjob.Resetable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.schedules.Interval;
import org.oddjob.state.CompleteOrNotOp;
import org.oddjob.state.State;
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
 * Keep checking for a file.
 * 
 * <pre>
 * &lt;scheduling:retry xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling"
 *                 xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
 *                 name="File Polling Example"&gt;
 *   &lt;schedule&gt;
 *       &lt;schedules:interval interval="00:00:05"/&gt;
 *   &lt;/schedule&gt;
 *   &lt;job&gt;
 *       &lt;sequential&gt;
 *           &lt;jobs&gt;
 *               &lt;exists id="check"
 *                       file="work/*.foo"/&gt;
 *               &lt;echo text="Found ${check.exists[0]}"/&gt;
 *           &lt;/jobs&gt;
 *       &lt;/sequential&gt;
 *   &lt;/job&gt;
 * &lt;/scheduling:retry&gt;
 * </pre>
 * 
 * @author Rob Gordon.
 */
public class Retry extends TimerBase {
	
	private static final long serialVersionUID = 2009091400L; 
	
	private Interval limits;
	
	@Override
	protected StateOperator getStateOp() {
		return new CompleteOrNotOp();
	}
	
	@Override
	protected void begin() throws Throwable {

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
	protected void rescheduleOn(State state) throws ComponentPersistException {
	    State completeOrNot = new CompleteOrNotOp().evaluate(state);
	    if (completeOrNot.isComplete()) {
	    	setNextDue(null);
	    }
	    else {

	    	Date use = getCurrent().getUseNext();
	    	Date now = getClock().getDate();
	    	if (use != null && use.before(now)) {
	    		use = now;
	    	}
	    	
	    	scheduleFrom(use);
	    }
	}
	
	@Override
	protected void reset(Resetable job) {
	    logger().debug("[" + this + "] Sending Soft Reset to [" + job + "]");
	    
    	job.softReset();
	}
	
}
