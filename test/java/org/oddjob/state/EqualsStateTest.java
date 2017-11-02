package org.oddjob.state;

import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.oddjob.OjTestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.tools.OurDirs;
import org.oddjob.tools.StateSteps;

public class EqualsStateTest extends OjTestCase {

   @Test
	public void testComplete() {
		
		EqualsState test = new EqualsState();
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
		
		job.setState(JobState.COMPLETE);
		
		test.softReset();
		
		assertEquals(JobState.READY, job.lastStateEvent().getState());
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
	}
	
   @Test
	public void testNotComplete() {
		
		EqualsState test = new EqualsState();
		test.setState(new IsNot(StateConditions.COMPLETE));
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		job.setState(JobState.COMPLETE);
		
		job.softReset();
		
		job.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
	}

   @Test
	public void testNotException() {
		
		EqualsState test = new EqualsState();
		test.setState(new IsNot(StateConditions.EXCEPTION));
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		job.setState(JobState.EXCEPTION);
		
		job.softReset();
		
		job.run();
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
	}
	
   @Test
	public void testInOddjob() {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:equals state='INCOMPLETE'>" +
			"   <job>" +
			"    <state:flag state='INCOMPLETE'/>" +
			"   </job>" +
			"  </state:equals>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));

		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
	}
	
   @Test
	public void testExample() throws InterruptedException, IOException, ArooaPropertyException, ArooaConversionException {
		
		OurDirs dirs = new OurDirs();
		
		File pretendLockFile = dirs.relative("work/pretend.lck");
		pretendLockFile.createNewFile();
		
		Properties properties = new Properties();
		properties.setProperty("db.lock.file", pretendLockFile.getPath());
		
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/state/EqualsStateExample.xml",
    			getClass().getClassLoader()));
    	oddjob.setProperties(properties);
    	
    	oddjob.load();
    	
    	SequentialJob sequential = new OddjobLookup(oddjob).lookup(
    			"db-backup", SequentialJob.class);
    	
    	StateSteps oddjobStates = new StateSteps(oddjob);
    	oddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
    			ParentState.ACTIVE, ParentState.STARTED);
    	
    	StateSteps sequentialStates = new StateSteps(sequential);
    	sequentialStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
    			ParentState.INCOMPLETE);
    	
    	oddjob.run();

    	oddjobStates.checkWait();
    	sequentialStates.checkNow();
    	
    	oddjobStates.startCheck( 
    			ParentState.STARTED, ParentState.ACTIVE, ParentState.COMPLETE);
    	
    	pretendLockFile.delete();
    	
    	oddjobStates.checkWait();
    	
    	oddjob.destroy();
	}
}
