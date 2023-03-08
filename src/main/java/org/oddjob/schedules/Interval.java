package org.oddjob.schedules;

import java.util.Date;

/**
 * An interval of time. An interval extends from and including the from
 * date up to but excluding the millisecond of the to date.
 * <p>
 * The smallest an interval can be is a millisecond.
 * 
 * @author rob
 *
 */
public interface Interval {

    /** The end of time. 31st December 9999 GMT */
	Date END_OF_TIME = new Date(253402214400000L);
    
    /** The start of time. 1st January -9999 GMT */
	Date START_OF_TIME = new Date(-377711769600000L);
    
    
    /**
     * The date the interval is from which includes this date.
     * 
     * @return A date. Never null and always less than or equal to the 
     * to date.
     */
	Date getFromDate();
	
	/**
	 * The date the interval is up to but excluding.
	 * 
	 * @return A date. Never null and always greater than or equal to the 
	 * from date.
	 */
	Date getToDate();
}
