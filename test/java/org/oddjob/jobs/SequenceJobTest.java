/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Helper;
import org.oddjob.framework.RunnableWrapper;
import org.oddjob.monitor.model.Describer;
import org.oddjob.state.JobState;

public class SequenceJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SequenceJobTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
	}

	public void testSerialize() throws Exception {
		SequenceJob test = new SequenceJob();
		test.setFrom(22);
		
		test.run();
		
		assertEquals(new Integer(22), test.getCurrent());
	
		SequenceJob copy = (SequenceJob) Helper.copy(test);
		
		assertEquals(new Integer(22), copy.getCurrent());
	}
	
	public void testSerializedByWrapper() throws Exception {
		SequenceJob test = new SequenceJob();
		test.setFrom(22);
		
		Runnable proxy = RunnableWrapper.wrapperFor(test, 
				getClass().getClassLoader());
		
		proxy.run();
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(proxy));
			
		Object copy = Helper.copy(proxy);
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(copy));
	}
	
	
	
	
	public void testDescribe() {
		SequenceJob test = new SequenceJob();
				
		test.run();
		
		Map<String, String> m = Describer.describe(test);
		
		String current = (String)m.get("current");
		assertEquals("0", current);
	}
}
