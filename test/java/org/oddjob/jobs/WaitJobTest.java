/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs;
import org.junit.Before;

import org.junit.Test;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;

import org.oddjob.OjTestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.state.FlagState;
import org.oddjob.state.IsNot;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.FragmentHelper;
import org.oddjob.tools.IconSteps;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

public class WaitJobTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(WaitJobTest.class);
	
    @Before
    public void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
	}
	
   @Test
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

   @Test
	public void testStateWait() throws InterruptedException {

		FlagState flag = new FlagState();
		flag.setState(JobState.COMPLETE);
		
		WaitJob test = new WaitJob();
		test.setFor(flag);
		test.setState(StateConditions.COMPLETE);
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(JobState.READY, JobState.EXECUTING);

		Thread t = new Thread(test);
		
		t.start();
		
		testStates.checkWait();

		flag.run();
		
		assertEquals(JobState.COMPLETE, flag.lastStateEvent().getState());
		
		t.join();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
	}
	
   @Test
	public void testStopStateWait() throws InterruptedException, FailedToStopException {
		FlagState sample = new FlagState();
		sample.setState(JobState.COMPLETE);
		sample.run();
		
		WaitJob test = new WaitJob();
		test.setFor(sample);
		test.setState(StateConditions.INCOMPLETE);
		test.setPause(9999999);
		
		IconSteps waitIcons = new IconSteps(test);
		waitIcons.startCheck("ready", "executing", "sleeping");
		
		Thread t = new Thread(test);
		
		t.start();
		
		waitIcons.checkWait();
		
		waitIcons.startCheck("sleeping", "executing", "sleeping");
		
		// Note we don't use run because that sends EXECUTING and COMPLETE
		// states. Just want to cause 1 state to check icon.
		sample.hardReset();
		
		waitIcons.checkWait();
		
		test.stop();
		
		waitIcons.startCheck("sleeping", "stopping", "complete");
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
	}
	
	
   @Test
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
	
   @Test
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
	
   @Test
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
			"        <schedules:interval interval='00:00:00.500'/>" +
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

//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oddjob);
//		explorer.run();
		
		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE, 
				ParentState.COMPLETE);
				
		oddjob.run();
		
		state.checkWait();
		
		oddjob.destroy();
		
		services.stop();
	}
	
   @Test
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
	
   @Test
	public void testWaitExample() throws InterruptedException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ArooaParseException {
		
		FragmentHelper helper = new FragmentHelper();
		Object sequential = helper.createComponentFromResource(
				"org/oddjob/jobs/WaitForExample.xml");
		
		Object[] children = OddjobTestHelper.getChildren(sequential);
		
		Object wait = children[1];
		
		StateSteps waitStates = new StateSteps((Stateful) wait);
		
		waitStates.startCheck(JobState.READY, JobState.EXECUTING);
	
		new Thread((Runnable) sequential).start();
		
		waitStates.checkWait();
		
		StateSteps echoStates = new StateSteps((Stateful) children[2]);
		
		echoStates.startCheck(JobState.READY, JobState.EXECUTING,
				JobState.COMPLETE);

		PropertyUtils.setProperty(children[0], "text", "Hello");

		echoStates.setTimeout(20000);
		echoStates.checkWait();
		
		String text = (String) BeanUtils.getProperty(children[2], "text");
		
		assertEquals("Hello", text);
	}
}
