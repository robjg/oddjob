package org.oddjob.schedules.schedules;

import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalHelper;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 * A help class for resolving a single parent/child schedule.
 * 
 * @author rob
 *
 */
public class ParentChildSchedule implements Schedule {

	private final Schedule parent;
	private final Schedule child;
	
	public ParentChildSchedule(Schedule parent, Schedule child) {
		this.parent = parent;
		this.child = child;
	}
	
	/**
	 * Provides the next due interval for the parent and child.
	 */
	public ScheduleResult nextDue(ScheduleContext context) {
		ScheduleResult parentResult = limitedParentResult(context);
		
		if (parentResult == null) {
			return null;
		}
				
		if (child == null) {
			return parentResult;
		}
		
		ScheduleResult childResult = childResult(context, parentResult);
		
		if (childResult != null) {
			if (childResult.getUseNext() == null) {
				return new SimpleScheduleResult(
						childResult, childResult.getToDate());
			}
			else {
				return childResult;
			}
		}
		
		parentResult = limitedParentResult(context.move(
				parentResult.getToDate()));
		
		if (parentResult == null) {
			return null;
		}

		childResult = childResult(context, parentResult);
		
		if (childResult != null && childResult.getUseNext() == null) {
				return new SimpleScheduleResult(
						childResult, childResult.getToDate());
		}
		else {
			return childResult;
		}
	}
	
	/**
	 * Find the next due parent interval for the current context
	 * and if one exists check it isn't limited by the context.
	 * 
	 * @param context
	 * 
	 * @return The next parent interval.
	 */
	private ScheduleResult limitedParentResult(ScheduleContext context) {
		
		ScheduleResult parentInterval = parent.nextDue(context);
		
		if (parentInterval == null) {
			return null;
		}

		if (context.getParentInterval() != null) {
		
			IntervalHelper contextParentIntervalHelper =
				new IntervalHelper(
						context.getParentInterval());
						
			if (contextParentIntervalHelper.limit(
							parentInterval) == null) {
				
				// One more try. Maybe the child interval was an eager refinement. 
				// I.e. one that spans midnight - so we want to give it a chance
				// to be an extended refinement.
				parentInterval = parent.nextDue(context.move(
						parentInterval.getToDate()));
				
				if (parentInterval == null) {
					return null;
				}
				
				if (contextParentIntervalHelper.limit(parentInterval) == null) {
					return null;
				}
			}
		}
		
		return parentInterval;
	}
	
	/**
	 * Calculate the next child interval.
	 * 
	 * @param context The current context.
	 * @param parentInterval The parent interval.
	 * 
	 * @return The next child interval.
	 */
	private ScheduleResult childResult(ScheduleContext context, 
			Interval parentInterval) {
		
		// if now is before the start of the next interval
		// pass the start of the next interval to the child,
		// otherwise use now to find the next child interval.
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
