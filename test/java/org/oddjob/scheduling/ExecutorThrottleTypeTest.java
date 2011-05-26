package org.oddjob.scheduling;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

public class ExecutorThrottleTypeTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ExecutorThrottleTypeTest.class);
	
	private class Capture implements StructuralListener, JobStateListener {

		Set<Stateful> ready = new HashSet<Stateful>();
		
		Set<Stateful> executing = new HashSet<Stateful>();
		
		Set<Stateful> complete = new HashSet<Stateful>();
		
		@Override
		public void jobStateChange(JobStateEvent event) {
			logger.info("Received: " + event);
			
			synchronized(this) {
				switch (event.getJobState()) {
					case READY:
						ready.add(event.getSource());
						break;
					case EXECUTING:
						ready.remove(event.getSource());
						executing.add(event.getSource());
						break;
					case COMPLETE:
						executing.remove(event.getSource());
						complete.add(event.getSource());
						break;
				    default:
				    	throw new RuntimeException("Unexpected " + 
				    			event.getJobState());
				}
				
				this.notifyAll();
			}
		}

		@Override
		public void childAdded(StructuralEvent event) {
			((Stateful) event.getChild()).addJobStateListener(this);
		}

		@Override
		public void childRemoved(StructuralEvent event) {
			((Stateful) event.getChild()).removeJobStateListener(this);
		}
		
	}
	
	public void testThrottleInParallel() throws InterruptedException, ArooaPropertyException, ArooaConversionException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/scheduling/ExecutorThrottleInParallel.xml", 
				getClass().getClassLoader()));
		
		StateSteps oddjobState = new StateSteps(oddjob);
		
		oddjobState.startCheck(JobState.READY, JobState.EXECUTING);
		
		new Thread(oddjob).start();
		
		oddjobState.checkWait();
		
		oddjobState.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Structural parallel = null;
		
		while (true) {
			parallel = lookup.lookup("parallel", Structural.class);
			if (parallel != null) {
				break;
			}
			logger.info("Waiting for parallel to be created.");
			Thread.sleep(500);
		}
		
		Capture capture = new Capture();
		
		parallel.addStructuralListener(capture);
		
		synchronized (capture) {
			while (capture.executing.size() < 2) {
				logger.info("Waiting for 2 EXECUTING.");
				capture.wait();
			}
		}			
		
		((Stoppable) capture.executing.iterator().next()).stop();
			
		synchronized (capture) {
			while (capture.complete.size() < 1) {
				logger.info("Waiting for 1 COMPLETE.");
				capture.wait();
			}
			
			while (capture.executing.size() < 2) {
				logger.info("Waiting for 2 EXECUTING.");
				capture.wait();
			}
		}	
		
		((Stoppable) capture.executing.iterator().next()).stop();
			
		synchronized (capture) {
			while (capture.complete.size() < 2) {
				logger.info("Waiting for 2 COMPLETE.");
				capture.wait();
			}
			
			while (capture.executing.size() < 2) {
				logger.info("Waiting for 2 EXECUTING.");
				capture.wait();
			}
		}
		
		((Stoppable) parallel).stop();
			
		synchronized (capture) {
			while (capture.complete.size() < 4) {
				logger.info("Waiting for 4 COMPLETE.");
				capture.wait();
			}
		}
		
		oddjobState.checkWait();
				
		oddjob.destroy();
	}
	
	public void testStopParallel() throws InterruptedException, ArooaPropertyException, ArooaConversionException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/scheduling/ExecutorThrottleInParallel.xml", 
				getClass().getClassLoader()));
		
		StateSteps oddjobState = new StateSteps(oddjob);
		
		oddjobState.startCheck(JobState.READY, JobState.EXECUTING);
		
		new Thread(oddjob).start();
		
		oddjobState.checkWait();
		
		oddjobState.startCheck(JobState.EXECUTING, JobState.READY);
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Structural parallel = null;
		
		while (true) {
			parallel = lookup.lookup("parallel", Structural.class);
			if (parallel != null) {
				break;
			}
			logger.info("Waiting for parallel to be created.");
			Thread.sleep(500);
		}
		
		Capture capture = new Capture();
		
		parallel.addStructuralListener(capture);
		
		synchronized (capture) {
			while (capture.executing.size() < 2) {
				logger.info("Waiting for 2 EXECUTING.");
				capture.wait();
			}
		}			
		
		((Stoppable) parallel).stop();
			
		oddjobState.checkWait();
		
		assertEquals(2, capture.complete.size());
		assertEquals(2, capture.ready.size());
			
		oddjob.destroy();
	}
}
