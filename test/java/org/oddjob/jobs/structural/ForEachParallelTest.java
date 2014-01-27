package org.oddjob.jobs.structural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Loadable;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.Configured;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.MockExecutorService;
import org.oddjob.scheduling.MockFuture;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

public class ForEachParallelTest extends TestCase {

	private static final Logger logger = Logger.getLogger(
			ForEachParallelTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("----------------------------------  " + getName() +
				"  ---------------------------------------");
	}
	
	public void testSimpleParallel() throws InterruptedException {
		
		DefaultExecutors defaultServices = new DefaultExecutors();
		
    	String xml =
			"<foreach id='test'>" +
			" <job>" +
    		"  <echo>${test.current}</echo>" +
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
    	
    	Object[] children = OddjobTestHelper.getChildren(test);
    	
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
    	
    	test.load();
    	
    	StateSteps state = new StateSteps(test);
    	state.startCheck(ParentState.READY, 
    			ParentState.EXECUTING, ParentState.ACTIVE);
    	    	
    	Object[] children = OddjobTestHelper.getChildren(test);
    	
    	assertEquals(10, children.length);
    	
    	StateSteps[] childChecks = new StateSteps[10];
    	for (int i = 0; i < 10; ++i) {
    		childChecks[i] = new StateSteps((Stateful) children[i]);
    		childChecks[i].startCheck(JobState.READY, JobState.EXECUTING);
    	}
    	
    	test.run();
    	
    	state.checkNow();
    	
    	// Ensure every child is executing before we stop them.
    	for (int i = 0; i < 10; ++i) {
    		childChecks[i].checkWait();
    	}
    	
    	state.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
    	
    	test.stop();
    	
    	state.checkNow();
    	
    	test.destroy();
    	
    	defaultServices.stop();
	}
	
	private static class MyExecutor extends MockExecutorService {
		
		List<Runnable> jobs = new ArrayList<Runnable>();
		
		int cancels;
		
		@Override
		public Future<?> submit(Runnable task) {
			jobs.add(task);
			return new MockFuture<Void>() {
				@Override
				public boolean cancel(boolean mayInterruptIfRunning) {
					++cancels;
					return false;
				}
			};
		}
	}
	
	public void testStopWithSlowStartingChild() throws InterruptedException, FailedToStopException {
		
		MyExecutor executor = new MyExecutor();
		
    	String xml =
			"<foreach id='test'>" +
			" <job>" +
    		"  <echo>${test.current}</echo>" +
    		" </job>" +
    		"</foreach>";

		ForEachJob test = new ForEachJob();
		test.setExecutorService(executor);
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
    	test.setArooaSession(session);
    	test.setConfiguration(new XMLConfiguration("XML", xml));
  
    	test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    	test.setParallel(true);
    	
    	StateSteps state = new StateSteps(test);
    	state.startCheck(ParentState.READY, 
    			ParentState.EXECUTING, ParentState.ACTIVE);
    	
    	test.run();
    	
    	state.checkNow();
    	
    	Object[] children = OddjobTestHelper.getChildren(test);
    	
    	assertEquals(10, children.length);
    	
    	assertEquals(10, executor.jobs.size());

    	// Only execute 5 jobs.
    	for (int i = 0; i < 5; ++i) {
    		executor.jobs.get(i).run();
    	}
    	
    	state.startCheck(ParentState.ACTIVE, ParentState.READY);
    	
    	test.stop();
    	
    	state.checkNow();
    	
    	assertEquals(10, executor.cancels);
    	
    	test.destroy();    	
	}
	
	public void testExampleInOddjob() throws InterruptedException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/ForEachParallelExample.xml",
				getClass().getClassLoader()));
		
		oddjob.load();
				
		Object foreach = OddjobTestHelper.getChildren(oddjob)[0];
		
		((Loadable) foreach).load();
		
		Object[] children = OddjobTestHelper.getChildren(foreach);
		
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
	
	static final int BIG_LIST_SIZE = 20;
	
	public static class BigList implements Iterable<Integer> {
		
		private int listSize = BIG_LIST_SIZE;
		
		List<Integer> theList = new ArrayList<Integer>();
		
		@Configured
		public void afterConfigure() {
			for (int i = 0; i < listSize; ++i) {
				theList.add(new Integer(i));
			}
		}
		
		@Override
		public Iterator<Integer> iterator() {
			return theList.iterator();
		}

		public int getListSize() {
			return listSize;
		}

		public void setListSize(int listSize) {
			this.listSize = listSize;
		}
	}
	
	private class ChildTracker implements StructuralListener {
		
		List<Object> children = new ArrayList<Object>();
		
		Exchanger<Stateful> lastChild;
				
		@Override
		public void childAdded(StructuralEvent event) {
			
			children.add(event.getIndex(), event.getChild());
						
			if (lastChild != null) {
				try {
					logger.info("* Waiting to Exchange " + 
							event.getChild().toString());
					
					lastChild.exchange((Stateful) event.getChild());
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		@Override
		public void childRemoved(StructuralEvent event) {
			children.remove(event.getIndex());
		}	
	}
		
	
	public void testParallelWithWindow() throws FailedToStopException, ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/ForEachParallelWithWindow.xml", 
				getClass().getClassLoader()));
		
		StateSteps oddjobState = new StateSteps(oddjob);
		oddjobState.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE, 
				ParentState.COMPLETE);
		
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Structural foreach = lookup.lookup("foreach",
				Structural.class);
		
		int preLoad = lookup.lookup("foreach.preLoad", int.class); 
		
		ChildTracker tracker = new ChildTracker();
		
		foreach.addStructuralListener(tracker);
		
		List<?> children = tracker.children;
		
		assertEquals(preLoad, children.size());
		
		while (((Stateful) children.get(
					children.size() - preLoad)).lastStateEvent().getState() 
					!= JobState.EXECUTING) {
			Thread.sleep(20);
		}
				
		tracker.lastChild = new Exchanger<Stateful>();
		
		for (int index = 1; index < BIG_LIST_SIZE - 1; ++index) {
			
			if (index < 5) {
				for (int i = 0; i < index; ++i) {
					assertEquals("Wait " + i, children.get(i).toString());
				}
			}
			else if (index < 1000) {
				for (int i = index - 4; i < index; ++i) {
					assertEquals("Wait " + i, children.get(i - (index - 4)).toString());
				}
			}
			
			((Stoppable) children.get(children.size() - 2)).stop();
			
			logger.info("* Waiting for new child after index " + index);
			
			Stateful lastChild = tracker.lastChild.exchange(null);
			
			while (lastChild.lastStateEvent().getState() 
					!= JobState.EXECUTING) {
				Thread.sleep(20);
			}
		}
		
		((Stoppable) children.get(children.size() - 2)).stop();
		((Stoppable) children.get(children.size() - 1)).stop();
		
		oddjobState.checkWait();
		
		oddjob.destroy();
	}
	
	public void testParallelWithWindowStop() throws FailedToStopException, ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/ForEachParallelWithWindow.xml", 
				getClass().getClassLoader()));
		
		oddjob.load();
		
		Stateful foreach = new OddjobLookup(oddjob).lookup("foreach",
				Stateful.class);
		
		((Loadable) foreach).load();
		
		Object[] children = OddjobTestHelper.getChildren(foreach);
		
		assertEquals(2, children.length);
		assertEquals("Wait 0", children[0].toString());
		assertEquals("Wait 1", children[1].toString());
		
		StateSteps wait1States = new StateSteps((Stateful) children[0]);
		StateSteps wait2States = new StateSteps((Stateful) children[1]);
		
		wait1States.startCheck(JobState.READY, JobState.EXECUTING);
		wait2States.startCheck(JobState.READY, JobState.EXECUTING);
		
		oddjob.run();
		
		wait1States.checkWait();
		wait2States.checkWait();
		
		((Stoppable) foreach).stop();
		
		assertEquals(ParentState.COMPLETE, 
				((Stateful) foreach).lastStateEvent().getState());
		
		 children = OddjobTestHelper.getChildren(foreach);
		
		assertEquals(2, children.length);
		assertEquals("Wait 0", children[0].toString());
		assertEquals("Wait 1", children[1].toString());
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
}
