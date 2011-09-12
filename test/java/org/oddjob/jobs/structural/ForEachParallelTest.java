package org.oddjob.jobs.structural;

import java.util.Arrays;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.Loadable;
import org.oddjob.Oddjob;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;

public class ForEachParallelTest extends TestCase {

	
	public void testSimpleParallel() throws InterruptedException {
		
		DefaultExecutors defaultServices = new DefaultExecutors();
		
    	String xml =
			"<foreach id='test'>" +
			" <job>" +
    		"  <echo text='${test.current}'/>" +
    		" </job>" +
    		"</foreach>";

		ForEachJob test = new ForEachJob();
		test.setExecutorService(defaultServices.getPoolExecutor());
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
    	test.setArooaSession(session);
    	test.setConfiguration(new XMLConfiguration("XML", xml));
  
    	test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    	test.setParallel(true);
    	
    	StateSteps state = new StateSteps(test);
    	state.startCheck(ParentState.READY, 
    			ParentState.EXECUTING, ParentState.ACTIVE, 
    			ParentState.COMPLETE);
    	test.run();
    	
    	Object[] children = Helper.getChildren(test);
    	
    	assertEquals(10, children.length);
    	
    	state.checkWait();
    	
    	test.destroy();
    	
    	defaultServices.stop();
	}
	
	public void testStop() throws InterruptedException, FailedToStopException {
		
		DefaultExecutors defaultServices = new DefaultExecutors();
		
    	String xml =
			"<foreach id='test'>" +
			" <job>" +
    		"  <wait name='${test.current}'/>" +
    		" </job>" +
    		"</foreach>";

		ForEachJob test = new ForEachJob();
		test.setExecutorService(defaultServices.getPoolExecutor());
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
    	test.setArooaSession(session);
    	test.setConfiguration(new XMLConfiguration("XML", xml));
  
    	test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    	test.setParallel(true);
    	
    	StateSteps state = new StateSteps(test);
    	state.startCheck(ParentState.READY, 
    			ParentState.EXECUTING, ParentState.ACTIVE);
    	
    	test.run();
    	
    	Object[] children = Helper.getChildren(test);
    	
    	assertEquals(10, children.length);
    	
    	state.checkWait();
    	
    	state.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
    	
    	test.stop();
    	
    	state.checkNow();
    	
    	test.destroy();
    	
    	defaultServices.stop();
	}
	
	public void testExampleInOddjob() throws InterruptedException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/ForEachParallelExample.xml",
				getClass().getClassLoader()));
		
		oddjob.load();
				
		Object foreach = Helper.getChildren(oddjob)[0];
		
		((Loadable) foreach).load();
		
		Object[] children = Helper.getChildren(foreach);
		
		StateSteps wait1 = new StateSteps((Stateful) children[0]);
		StateSteps wait2 = new StateSteps((Stateful) children[1]);
		StateSteps wait3 = new StateSteps((Stateful) children[2]);
		
		wait1.startCheck(JobState.READY, JobState.EXECUTING);
		wait2.startCheck(JobState.READY, JobState.EXECUTING);
		wait3.startCheck(JobState.READY, JobState.EXECUTING);
		
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());
		
		wait1.checkWait();
		wait2.checkWait();
		wait3.checkWait();
						
		oddjob.stop();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
}
