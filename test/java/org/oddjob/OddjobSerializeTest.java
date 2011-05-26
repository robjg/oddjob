package org.oddjob;

import java.io.IOException;

import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

import junit.framework.TestCase;

public class OddjobSerializeTest extends TestCase {
	
	public void testSerializeAndReRun() throws IOException, ClassNotFoundException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <echo id='e' text='Apples'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		assertEquals("Apples", new OddjobLookup(test).lookup("e.text"));

		Oddjob copy = Helper.copy(test);
		
		assertEquals(JobState.COMPLETE, copy.lastJobStateEvent().getJobState());
		assertEquals(null, new OddjobLookup(copy).lookup("e"));
		
		copy.load();
		
		OddjobLookup copyLookup = new OddjobLookup(copy);
		
		assertEquals("Apples", copyLookup.lookup("e.text"));
		
		Object echo = copyLookup.lookup("e");
		
		assertEquals(JobState.READY, Helper.getJobState(echo));
	}
	
	public void testSerializeWhenReset() throws IOException, ClassNotFoundException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <echo id='e' text='Apples'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());

		test.hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		Oddjob copy = Helper.copy(test);
		copy.setConfiguration(new XMLConfiguration("XML", xml));
		assertEquals(JobState.READY, copy.lastJobStateEvent().getJobState());
		
		copy.run();
		
		OddjobLookup copyLookup = new OddjobLookup(copy);
		
		assertEquals("Apples", copyLookup.lookup("e.text"));
		
		Object echo = copyLookup.lookup("e");
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(echo));
	}
	
	public void testSerializeNoConfig() throws IOException, ClassNotFoundException {
		
		Oddjob test = new Oddjob();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		Oddjob copy = Helper.copy(test);
		
		assertEquals(JobState.READY, copy.lastJobStateEvent().getJobState());
	}
}
