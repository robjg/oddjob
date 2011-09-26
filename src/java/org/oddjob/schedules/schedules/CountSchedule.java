package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleInterval;

/**
 * @oddjob.description This schedule returns up to count 
 * number of child schedules. It is typically
 * used to count a number intervals for re-trying something.
 * <p>
 * If there is more than one count in a schedule a key must be provided
 * to differentiate their internally store numbers, otherwise the count value
 * would be shared.
 * <p>
 * If the nested schedule isn't specified it defaults to 
 * {@link org.oddjob.schedules.schedules.NowSchedule}
 * 
 * @oddjob.example
 * 
 * A schedule for 5 times at intervals of 15 minutes.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/CountExample.xml}
 * 
 * @oddjob.example
 * 
 * A schedule for 3 times each day at 5 minute intervals.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/CountDaily.xml}
 * 
 * @oddjob.example
 * 
 * Nested count schedules. This slightly contrived example would cause a timer
 * to run a job twice at 1 minute intervals for 3 days. Note that we need a
 * key on one of the schedules to differentiate them.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/CountDifferentCounts.xml}
 * 
 * @author Rob Gordon
 */
final public class CountSchedule extends AbstractSchedule 
implements Serializable {
    private static final long serialVersionUID = 20050226;
    
    private static final Logger logger = Logger.getLogger(CountSchedule.class);
    
    private static final String COUNT_KEY = "countschedulecount";
    
    /**
     * @oddjob.property count
     * @oddjob.description The number to count to.
     * @oddjob.required Yes.
     */
    private int countTo;
	
    /**
     * @oddjob.property 
     * @oddjob.description If there are more than one count schedules in a
     * schedule then this key is required to differentiate them. It can be any
     * text.
     * @oddjob.required No.
     */
    private String identifier;
    
    /**
     * Bean constructor.
     */
    public CountSchedule() {
	}
    
    /**
     * Constructor with count.
     * 
     * @param countTo
     */
    public CountSchedule(int countTo) {
    	this.countTo = countTo;
	}

	/**
	 * Set the number to count to.
	 * 
	 * @param count The number to count to.
	 */
	public void setCount(int count) {	
		this.countTo = count;
	}

	/**
	 * Get the number to count to.
	 * 
	 * @return The number to count to.
	 */
	public int getCount() {	
		return countTo;
	}
	
	/**
	 * Getter for key.
	 * 
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Setter for key.
	 * 
	 * @param key
	 */
	public void setIdentifier(String key) {
		this.identifier = key;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.treesched.Schedule#nextDue(java.util.Date)
	 */
	public ScheduleResult nextDue(ScheduleContext context) {
		if (context.getDate() == null) {
			return null;
		}
		
		Schedule child = getRefinement();
		if (child == null) {
		    child = new NowSchedule();
		}

		String countKey = COUNT_KEY;
		if (identifier != null) {
			countKey +=identifier;
		}
		
		IntervalCounts storedCount = (IntervalCounts) context.getData(countKey);
		
		if (storedCount == null) {
			storedCount = new IntervalCounts();
			context.putData(countKey, storedCount);
		}
		
		Interval parent = context.getParentInterval();
		
		int counted = storedCount.retrieve(parent);
		
		logger.debug(this + ", count is " + counted);

		++counted;
		
		storedCount.store(parent, counted);
		
		if ( counted <= countTo) {
			return child.nextDue(context);
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
	
	private static class IntervalCounts implements Serializable {

		private static final long serialVersionUID = 2011092600L;
		
		private final static Interval NULL_INTERVAL = 
			new SimpleInterval(Interval.START_OF_TIME, Interval.END_OF_TIME);
		
		private final Map<Interval, Integer> counts = 
			new LinkedHashMap<Interval, Integer>(5);
		
		void store(Interval interval, int count) {
			
			if (interval == null) {
				interval = NULL_INTERVAL;
			}
			
			counts.put(interval, new Integer(count));
			
			// Stop keeping hundreds of past intervals which 
			// would cause a memory leak
			if (counts.size() > 10) {
				Interval first = counts.keySet().iterator().next();
				counts.remove(first);
			}
		}
		
		int retrieve(Interval interval) {
			
			if (interval == null) {
				interval = NULL_INTERVAL;
			}
			
			Integer retrieved = counts.get(interval);
			
			if (retrieved == null) {
				return 0;
			}
			else {
				return retrieved.intValue();
			}
		}
		
	}
}
