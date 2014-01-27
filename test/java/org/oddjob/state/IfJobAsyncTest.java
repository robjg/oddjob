package org.oddjob.state;

import java.io.File;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.tools.StateSteps;

public class IfJobAsyncTest extends TestCase {

	public void testAsyncCompletionWithThenJob() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		File file = new File(getClass().getResource(
				"IfJobAsyncThen.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful ifJob = lookup.lookup("if-job", Stateful.class);
		
		StateSteps ifJobStates = new StateSteps(ifJob);
		ifJobStates.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE, ParentState.COMPLETE);
		
		oddjob.run();
		
		ifJobStates.checkWait();
		
		Stateful thenJob = lookup.lookup("then-job", Stateful.class);
		Stateful elseJob = lookup.lookup("else-job", Stateful.class);
		
		assertEquals(JobState.COMPLETE, thenJob.lastStateEvent().getState());
		assertEquals(JobState.READY, elseJob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	public void testAsyncCompletionWithElseJob() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		File file = new File(getClass().getResource(
				"IfJobAsyncElse.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful ifJob = lookup.lookup("if-job", Stateful.class);
		
		StateSteps ifJobStates = new StateSteps(ifJob);
		ifJobStates.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE, ParentState.COMPLETE);
		
		oddjob.run();
		
		ifJobStates.checkWait();
		
		Stateful thenJob = lookup.lookup("then-job", Stateful.class);
		Stateful elseJob = lookup.lookup("else-job", Stateful.class);
		
		assertEquals(JobState.READY, thenJob.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, elseJob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	public void testAsyncStop() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
		File file = new File(getClass().getResource(
				"IfJobAsyncStop.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful ifJob = lookup.lookup("if-job", Stateful.class);
		
		StateSteps ifJobStates = new StateSteps(ifJob);
		ifJobStates.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE);
		
		oddjob.run();
		
		ifJobStates.checkWait();
		
		ifJobStates.startCheck(ParentState.ACTIVE, ParentState.READY);
		
		((Stoppable) ifJob).stop();
		
		ifJobStates.checkWait();
		
		Stateful thenJob = lookup.lookup("then-job", Stateful.class);
		Stateful elseJob = lookup.lookup("else-job", Stateful.class);
		
		assertEquals(JobState.READY, thenJob.lastStateEvent().getState());
		assertEquals(JobState.READY, elseJob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
}
