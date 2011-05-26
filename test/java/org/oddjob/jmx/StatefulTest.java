package org.oddjob.jmx;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

import junit.framework.TestCase;

public class StatefulTest extends TestCase {

	class Result implements JobStateListener {
		JobStateEvent event;
		
		public void jobStateChange(JobStateEvent event) {
			this.event = event;
			synchronized (this) {
				notifyAll();
			}
		}
	}
	
	
	public void testState() throws ArooaConversionException, InterruptedException {
		
		String xml =
			"<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <rmireg />" +
			"    <jmx:server id='server1'" +
			"            url='service:jmx:rmi://ignored/jndi/rmi://localhost/server1'" +
			"            root='${fruit}' />" +
			" 	 <echo id='fruit' text='apples' />" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));

		oddjob.run();
		
		String address = new OddjobLookup(
				oddjob).lookup("server1.address", String.class);
		
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.setUrl(address);
		
		client.run();
		
		RemoteDirectory remote = client.provideBeanDirectory();
		
		Stateful fruit = (Stateful) remote.lookup("fruit", Stateful.class);
		
		assertNotNull(fruit);
		
		Result result = new Result();

		fruit.addJobStateListener(result);
		
		assertEquals(JobState.COMPLETE, result.event.getJobState());

		Resetable resetable = (Resetable) fruit;
		
		resetable.hardReset();

		synchronized (result) {
			result.wait(5000);
		}
		assertEquals(JobState.READY, result.event.getJobState());

		client.destroy();
		
		oddjob.destroy();
	}
}
