package org.oddjob.schedules;

import java.util.Date;

/**
 * Utility class to role a schedule and provide the 
 * resultant intervals.
 * 
 * @author rob
 *
 */
public class ScheduleRoller {

	private final Schedule schedule;
	
	private final int howMany;
	
	public ScheduleRoller(Schedule schedule) {
		this(schedule, 10);
	}
	
	public ScheduleRoller(Schedule schedule, int howMany) {
		this.schedule = schedule;
		this.howMany = howMany;
	}
	
	public ScheduleResult[] resultsFrom(Date date) {
		
		ScheduleResult[] results = new ScheduleResult[howMany];
	
		ScheduleContext context = new ScheduleContext(date);
		
		for (int i = 0; i < howMany; ++i) {
			
			ScheduleResult result = schedule.nextDue(context); 
			
			if (result == null) {
				break;
			}
			
			results[i] = result;
			
			context = context.move(result.getUseNext());
		}
		
		return results;
	}
}
