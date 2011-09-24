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
    public static final Date END_OF_TIME = new Date(253402214400000L);
    
    /** The start of time. 1st January -9999 GMT */
    public static final Date START_OF_TIME = new Date(-377711769600000L);
    
    
    /**
     * The date the interval is from which includes this date.
     * 
     * @return Never null and alway less than the to date.
     */
	public Date getFromDate();
	
	/**
	 * The date the interval is up to but excluding.
	 * 
	 * @return A date. Never and always greater than the from date.
	 */
	public Date getToDate();
}
