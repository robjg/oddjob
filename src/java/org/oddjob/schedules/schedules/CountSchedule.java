package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;

/**
 * @oddjob.description This schedule returns up to count 
 * number of child schedules. It is typically
 * used to count a number intervals for re-trying something, But could be 
 * used to count
 * the first n days of the month for instance.
 * <p>
 * The count will be reset if the end interval changes, i.e. when counting 
 * n days of the month,
 * and the month moves on. However in the retry situation 
 * to reset the count schedule it would
 * need to be reset.
 * <p>
 * If the nested schedule isn't specified it defaults to 
 * {@link org.oddjob.schedules.schedules.NowSchedule}
 * 
 * @oddjob.example
 * 
 * This would shcedule a job 5 times at intervals of 15 minutes.
 * 
 * <pre>
 * &lt;schedules:count xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
 *           count="5"&gt;
 *   &lt;refinement&gt;
 *     &lt;schedules:interval interval="00:15"/&gt;
 *   &lt;/refinement&gt;
 * &lt;/schedules:count&gt;
 * </pre>
 * 
 * @author Rob Gordon
 */

final public class CountSchedule extends AbstractSchedule 
implements Serializable {
    private static final long serialVersionUID = 20050226;
    
    private static final Logger logger = Logger.getLogger(CountSchedule.class);
    
    private static final String COUNT_KEY = "countschedulecount";
    private static final String LAST_KEY = "countschedulelast";
    
    /**
     * @oddjob.property count
     * @oddjob.description The number to count to.
     * @oddjob.required Yes.
     */
    private int countTo;
	
    
    public CountSchedule() {
	}
    
    public CountSchedule(int countTo) {
    	this.countTo = countTo;
	}

	/**
	 * Set the number to count to.
	 * 
	 * @param count The number to count to.
	 */
	public void setCount(String count) {	
		this.countTo = Integer.parseInt(count);
	}

	/**
	 * Get the number to count to.
	 * 
	 * @return The number to count to.
	 */
	public String getCount() {	
		return Integer.toString(countTo);
	}

	class ImmediateSchedule implements Schedule, Serializable {
		private static final long serialVersionUID = 20060113;

		@Override
		public IntervalTo nextDue(ScheduleContext context) {
			Date date = context.getDate();
			return new IntervalTo(date);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.Schedule#nextDue(java.util.Date)
	 */
	public IntervalTo nextDue(ScheduleContext context) {
		Date now = context.getDate();
		if (now == null) {
			return null;
		}
		if (getRefinement() == null) {
		    setRefinement(new ImmediateSchedule());
		}

		int counted = 0;
		Integer storedCount = (Integer) context.getData(COUNT_KEY);
		if (storedCount != null) {
			counted = storedCount.intValue();
		}
		
		logger.debug(this + ": in date is " + now + ", count is " + counted);
		
		Date last = (Date) context.getData(LAST_KEY);
		
		if (!now.equals(last)) {
		    ++counted;
		    last = now;
		}

		context.putData(COUNT_KEY, new Integer(counted));
		context.putData(LAST_KEY, last);
		
		if ( counted <= countTo) {
			Schedule child = getRefinement();
			IntervalTo next = child.nextDue(context);
			
			IntervalTo parentInterval = context.getParentInterval();
			if (parentInterval == null) {
				return next;
			}
			else {
				return parentInterval.limit(next);
			}
		}
		else {
			return null;			
		}
	}
	
	/**
	 * Override toString to be more meaningful.
	 */

	public String toString() {
		return "Count Schedule, count to " + countTo;
	}
}
