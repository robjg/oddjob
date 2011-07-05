package org.oddjob.schedules.regression;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;

/**
 * A single schedule run.
 */
public class TestScheduleRun {

	private static final Logger logger = Logger.getLogger(
			TestScheduleRun.class);
	
	/** Date running for. */
	private String testDate;
	
    private String expectedFrom;
    
    private String expectedTo;
	
    public void setExpectedFrom(String from) {
        this.expectedFrom = from;
    }
    
    public String getExpectedFrom() {
        return expectedFrom;
    }
    
    public void setExpectedTo(String to) throws ParseException {
        this.expectedTo = to;
    }
    
    public String getExpectedTo() {
        return expectedTo;    
    }
    
	IntervalTo getExpected() {
		if (this.expectedFrom == null && this.expectedTo == null) {
			return null;
		}
		
		
	    try {
	    	if (this.expectedTo == null) {
		    	return new IntervalTo(
		    			DateHelper.parseDateTime(this.expectedFrom));
	    	}
	    	else {	    		
		    	return new IntervalTo(
		    			DateHelper.parseDateTime(this.expectedFrom), 
		    			DateHelper.parseDateTime(this.expectedTo));
	    	}
	    } catch (ParseException e) {
	    	throw new RuntimeException(e);
	    }
	}
	
	public void setDate(String date)  {
		this.testDate = date;
	}
	
	public String getDate() {
	    return testDate;
	}
	
	public void testSchedule(Schedule schedule) throws Exception {
		Date date = null;
		try {
			date = DateHelper.parseDateTime(testDate);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

//		DateFormat format = new SimpleDateFormat("dd-MMM-yy HH:mm:ss:SSS");
		IntervalTo nextDue = schedule.nextDue(
				new ScheduleContext(date));
		
		IntervalTo expected = getExpected();
		
		logger.info("Given [" + testDate
				+ "]: next due " + nextDue + ", expected - " + expected);
					
		if (expected == null) {
			return;
		}

		if (!expected.equals(nextDue)) {
			throw new Exception("For date: " + date + ", Expected: " + expected + ", Actual: " + nextDue); 
		}

	}
}
