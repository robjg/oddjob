/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.FragmentHelper;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.state.FlagState;
import org.oddjob.state.IsNot;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;

public class WaitJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(WaitJobTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
	}
	
	public void testInOddjob() throws Exception {
	
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(
				new XMLConfiguration("Resource",
						this.getClass().getResourceAsStream("wait.xml")));

		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE, 
				ParentState.COMPLETE);
		
		oddjob.run();

		state.checkWait();
		
		Object result =  new OddjobLookup(oddjob).lookup("test");
		assertEquals("hello", PropertyUtils.getProperty(result, "text"));
		
		oddjob.destroy();
	}

	public void testStateWait() {
		FlagState sample = new FlagState();
		sample.setState(JobState.COMPLETE);
		
		WaitJob wait = new WaitJob();
		wait.setPause(2000);
		
		SequentialJob sequential = new SequentialJob();
		sequential.setJobs(0, wait);
		sequential.setJobs(1, sample);
		
		Thread t = new Thread(sequential);
		
		WaitJob test = new WaitJob();
		test.setFor(sample);
		test.setState(StateConditions.COMPLETE);

		t.start();
		test.run();
		
		assertEquals(JobState.COMPLETE, sample.lastStateEvent().getState());
	}
	
	public void testStateWaitInOJ() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("Resource",
				WaitJobTest.class.getResourceAsStream("wait2.xml")));

		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE, 
				ParentState.COMPLETE);
		
		oddjob.run();

		state.checkWait();

		Object result =  new OddjobLookup(oddjob).lookup("test");
		assertEquals("hello", PropertyUtils.getProperty(result, "text"));
		
		oddjob.destroy();
	}
	
	public void testNotStateWait() {
		FlagState sample = new FlagState();
		sample.setState(JobState.COMPLETE);
				
		WaitJob test = new WaitJob();
		test.setFor(sample);
		test.setState(new IsNot(StateConditions.COMPLETE));

		assertEquals(JobState.READY, sample.lastStateEvent().getState());
		
		// smample will still be ready
		test.run();
		
	}
	
	public void testSimpleStop() throws Exception {

		String xml = 
			"<oddjob xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'" +
			"        xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'" +
			"        xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <parallel>" +
			"   <jobs>" +
			"    <scheduling:trigger state='EXECUTING' on='${wait}'>" +
			"     <job>" +
			"      <scheduling:retry>"  +
			"       <schedule>" +
			"        <schedules:interval interval='00:01'/>" +
			"       </schedule>" +
			"       <job>" +
			"        <sequential>" +
			"         <jobs>" +
			"          <stop job='${wait}'/>" +
			"          <state:mirror job='${wait}'/>" +
			"         </jobs>" +
			"        </sequential>" +
			"       </job>" +
			"      </scheduling:retry>" +
			"     </job>" +
			"    </scheduling:trigger>" +
			"    <wait id='wait'/>" +
			"   </jobs>" +
			"  </parallel>" +
			" </job>" +
			"</oddjob>";
		
		DefaultExecutors services = new DefaultExecutors();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setOddjobExecutors(services);
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();

//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oddjob);
//		explorer.run();
//		
		WaitJob wait = new WaitJob();
		wait.setState(StateConditions.COMPLETE);
		wait.setFor(oddjob);
		
		wait.run();
		
		oddjob.destroy();
		
		services.stop();
	}
	
	public void testStateStop() throws PropertyVetoException, InterruptedException {

		String xml = 
			"<oddjob xmlns:s='http://rgordon.co.uk/oddjob/schedules'" +
			"        xmlns:state='http://rgordon.co.uk/oddjob/state'" +
			"   id='this'>" +
			" <job>" +
			"  <parallel>" +
			"   <jobs>" +
			"    <sequential>" +
			"     <jobs>" +
			"      <wait for='${wait}' state='EXECUTING'/>" +
			"      <stop job='${wait}'/>" +
			"     </jobs>" +
			"    </sequential>" +
			"    <wait id='wait' for='${this}' state='COMPLETE'/>" +
			"   </jobs>" +
			"  </parallel>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oddjob);
//		explorer.run();
		
		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE, 
				ParentState.COMPLETE);
		
		oddjob.run();

		state.checkWait();
		
		assertNotNull(new OddjobLookup(oddjob).lookup("wait.for"));
		
		oddjob.destroy();		
	}
	
	public void testWaitExample() throws InterruptedException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ArooaParseException {
		
		FragmentHelper helper = new FragmentHelper();
		Object sequential = helper.createComponentFromResource(
				"org/oddjob/jobs/WaitForExample.xml");
		
		Object[] children = Helper.getChildren(sequential);
		
		Object wait = children[1];
		
		StateSteps waitStates = new StateSteps((Stateful) wait);
		
		waitStates.startCheck(JobState.READY, JobState.EXECUTING);
	
		new Thread((Runnable) sequential).start();
		
		waitStates.checkWait();
		
		StateSteps echoStates = new StateSteps((Stateful) children[2]);
		
		echoStates.startCheck(JobState.READY, JobState.EXECUTING,
				JobState.COMPLETE);

		BeanUtils.setProperty(children[0], "text", "Hello");

		echoStates.setTimeout(20000);
		echoStates.checkWait();
		
		String text = (String) BeanUtils.getProperty(children[2], "text");
		
		assertEquals("Hello", text);
	}
}
