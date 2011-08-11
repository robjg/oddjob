/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs.job;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

/**
 * 
 */
public class RunJobTest extends TestCase {
	
	public static class OurRunnable implements Runnable {
		boolean ran;
		public void run() {
			ran = true;
		}
		public boolean isRan() {
			return ran;
		}
	}
	
	public void testCode() {
		OurRunnable r = new OurRunnable();
		
		RunJob j = new RunJob();
		j.setJob(r);
		j.run();
		
		assertEquals(JobState.COMPLETE
				, j.lastStateEvent().getState());
		assertTrue(r.ran);
	}
	
	public void testInOddjob() throws Exception {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <bean id='r' class='" + OurRunnable.class.getName() + "'/>" +
			"    <run job='${r}' />" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		Object r = new OddjobLookup(oj).lookup("r");
		assertEquals(new Boolean(true), PropertyUtils.getProperty(r, "ran"));
	}
}
