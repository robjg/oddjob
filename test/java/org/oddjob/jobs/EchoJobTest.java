/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

public class EchoJobTest extends TestCase {

	public void testInOddjob1() throws Exception {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <echo id='e' text='Hello' />" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		
		Object test = new OddjobLookup(oj).lookup("e");
		assertEquals(JobState.COMPLETE, Helper.getJobState(test));
		assertEquals("Hello", PropertyUtils.getProperty(test, "text"));
	}
	
	public void testInOddjob2() throws Exception {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <echo id='e' text='Hello' />" +
			"    <echo id='2' text='${e.text}' />" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		
		Object test = new OddjobLookup(oj).lookup("2");
		assertEquals(JobState.COMPLETE, Helper.getJobState(test));
		assertEquals("Hello", PropertyUtils.getProperty(test, "text"));
	}
}
