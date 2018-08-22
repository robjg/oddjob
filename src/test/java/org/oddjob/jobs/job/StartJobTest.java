/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs.job;

import org.junit.Test;

import java.util.Properties;

import org.oddjob.OjTestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;

/**
 * 
 */
public class StartJobTest extends OjTestCase {
	
	public static class OurRunnable implements Runnable {
		boolean ran;
		public void run() {
			ran = true;
		}
		public boolean isRan() {
			return ran;
		}
	}
	
   @Test
	public void testCode() {
		OurRunnable r = new OurRunnable();
		
		StartJob j = new StartJob();
		j.setJob(r);
		j.run();
		
		assertEquals(JobState.COMPLETE
				, j.lastStateEvent().getState());
		assertTrue(r.ran);
	}
	
   @Test
	public void testInOddjob() throws Exception {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <bean id='r' class='" + OurRunnable.class.getName() + "'/>" +
			"    <start job='${r}' />" +
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
	
   @Test
	public void testExample() {

		Properties properties = new Properties();
		properties.setProperty("priceService", "nonCachingPriceService");
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/job/StartJobExample.xml",
				getClass().getClassLoader()));
		oddjob.setProperties(properties);
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(lookup.lookup("nonCachingPriceService"),
				lookup.lookup("pricingJob.priceService"));
	}
}
