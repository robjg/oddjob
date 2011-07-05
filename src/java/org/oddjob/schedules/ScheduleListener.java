/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules;

import java.util.Date;

/**
 * 
 */
public interface ScheduleListener {

	/**
	 * Called when a ScheduleCalculator is first initialised.
	 * 
	 * @param scheduleDate The date the schedule is first due.
	 */
	public void initialised(Date scheduleDate);
	
	/**
	 * Called when the job has completed successfully.
	 *  
	 * @param scheduleDate The new schedule date.
	 * @param lastComplete The interval of the last schedule.
	 */
	public void complete(Date scheduleDate, IntervalTo lastComplete);
	
	/**
	 * The job has failed an the retry schedule is in
	 * operation.
	 * 
	 * @param scheduleDate The date when the job was due to be scheduled.
	 * This shouldn't have changed. Not sure why we need it here.
	 * 
	 * @param retryDate When a retry is due according to the 
	 * retry schedule.
	 */
	public void retry(Date scheduleDate, Date retryDate);
	
	/**
	 * Retries (if they exist) are exhausted.
	 * 
	 * @param scheduleDate When the job is next due according to 
	 * the schedule or null if it's not.
	 */
	public void failed(Date scheduleDate);
}
