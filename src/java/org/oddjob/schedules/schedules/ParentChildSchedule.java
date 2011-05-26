package org.oddjob.schedules.schedules;

import org.oddjob.schedules.DateUtils;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;

public class ParentChildSchedule implements Schedule {

	private final Schedule parent;
	private final Schedule child;
	
	public ParentChildSchedule(Schedule parent, Schedule child) {
		this.parent = parent;
		this.child = child;
	}
	
	public IntervalTo nextDue(ScheduleContext context) {
		IntervalTo parentInterval = limitedParentInterval(context);
		
		if (parentInterval == null) {
			return null;
		}
				
		if (child == null) {
			return parentInterval;
		}
		
		IntervalTo childInterval = childInterval(context, parentInterval);
		
		if (childInterval != null) {
			return childInterval;
		}
		
		parentInterval = limitedParentInterval(context.move(
				DateUtils.oneMillisAfter(parentInterval.getToDate())));
		
		if (parentInterval == null) {
			return null;
		}

		return childInterval(context, parentInterval);
	}
	
	private IntervalTo limitedParentInterval(ScheduleContext context) {
		
		IntervalTo parentInterval = parent.nextDue(context);
		
		if (parentInterval == null) {
			return null;
		}

		if (context.getParentInterval() != null) {
		
			if (context.getParentInterval().limit(parentInterval) == null) {
				
				// One more try. Maybe the child interval was an eager refinement. 
				// I.e. one that spans midnight - so we want to give it a chance
				// to be an extended refinement.
				parentInterval = parent.nextDue(context.move(
						DateUtils.oneMillisAfter(parentInterval.getToDate())));
				
				if (parentInterval == null) {
					return null;
				}
				
				if (context.getParentInterval().limit(parentInterval) == null) {
					return null;
				}
			}
		}
		
		return parentInterval;
	}
	
	private IntervalTo childInterval(ScheduleContext context, 
			IntervalTo parentInterval) {
		
		// if now is before the start of the next interval
		// pass the start of the next interval to the child.
		if (context.getDate().compareTo(parentInterval.getFromDate()) < 0) {
		    return child.nextDue(
		    		context.spawn(
		    				parentInterval.getFromDate(), 
		    				parentInterval));
		}
		else {
		    return child.nextDue(
		    		context.spawn(
		    				parentInterval));
		}
	}
}
