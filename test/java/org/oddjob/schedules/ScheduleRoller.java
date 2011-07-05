package org.oddjob.schedules;

import java.util.Date;

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
	
	public IntervalTo[] resultsFrom(Date date) {
		
		IntervalTo[] results = new IntervalTo[howMany];
	
		ScheduleContext context = new ScheduleContext(date);
		
		for (int i = 0; i < howMany; ++i) {
			
			IntervalTo result = schedule.nextDue(context); 
			
			if (result == null) {
				break;
			}
			
			results[i] = result;
			
			context = context.move(result.getUpToDate());
		}
		
		return results;
	}
}
