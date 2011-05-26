package org.oddjob.schedules.regression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.schedules.Schedule;

/**
 * 
 */
public class SingleTestSchedule {

    private static Logger logger = Logger.getLogger(SingleTestSchedule.class); 

    /** The schedule. */
    private Schedule schedule;
    
	private List<TestScheduleRun> runs = 
		new ArrayList<TestScheduleRun>();
	
	private String name;

	public void setName(String name) {
	    this.name = name;
	}
	
	public String getName() {
	    return this.name;
	}
	
	public void setRuns(int index, TestScheduleRun run) throws Exception {
		runs.add(run);
	}
	
	public void setSchedule(Schedule schedule) 
	throws NoConversionAvailableException, ConversionFailedException {
	    this.schedule = schedule;
	}
	
	public int countTestCases() {
		
		return runs.size();
	}

	public void run() {
		
		logger.info("Running ScheduleTest: " + name);
	
		for (Iterator<TestScheduleRun> it = runs.iterator(); it.hasNext(); ) {
			TestScheduleRun test = it.next();
			
			try {
				test.testSchedule(schedule);
			} catch (Exception e) {
				throw new RuntimeException("Test [" + name + "] failed.", e);
			}
		}
		
	}

}
