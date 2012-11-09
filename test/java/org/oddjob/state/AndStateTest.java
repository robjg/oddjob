package org.oddjob.state;


import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.oddjob.FragmentHelper;
import org.oddjob.OurDirs;
import org.oddjob.StateSteps;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.framework.ServicesJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.MockScheduledExecutorService;

public class AndStateTest extends TestCase {

	private class Result implements StateListener {
		State result;
		
		public void jobStateChange(StateEvent event) {
			result = event.getState();
		}
	}
	
	private class UnusedServices 
	extends MockScheduledExecutorService {
		
	}
	
	
	public void testComplete() {
		
		AndState test = new AndState();
		test.setExecutorService(new UnusedServices());
		test.run();
		
		Result listener = new Result();
		test.addStateListener(listener);

		
		assertEquals(ParentState.READY, listener.result);
		
		FlagState j1 = new FlagState(JobState.COMPLETE);

		test.setJobs(0, j1);
		
		assertEquals(ParentState.READY, listener.result);
		
		j1.run();
		
		assertEquals(ParentState.COMPLETE, listener.result);
		
		FlagState j2 = new FlagState(JobState.COMPLETE);

		test.setJobs(0, j2);
		
		assertEquals(ParentState.READY, listener.result);
		
		j2.run();
		
		assertEquals(ParentState.COMPLETE, listener.result);
		
		test.setJobs(1, null);
		
		assertEquals(ParentState.COMPLETE, listener.result);
		
		test.setJobs(0, null);
		
		assertEquals(ParentState.READY, listener.result);
	}
	
	public void testException() {
		
		AndState test = new AndState();
		test.setExecutorService(new UnusedServices());
		test.run();
		
		Result listener = new Result();
		test.addStateListener(listener);

		assertEquals(ParentState.READY, listener.result);
		
		FlagState j1 = new FlagState(JobState.COMPLETE);

		test.setJobs(0, j1);
		
		assertEquals(ParentState.READY, listener.result);
		
		j1.run();
		
		assertEquals(ParentState.COMPLETE, listener.result);
		
		FlagState j2 = new FlagState(JobState.EXCEPTION);

		test.setJobs(0, j2);
		
		assertEquals(ParentState.READY, listener.result);
		
		j2.run();
		
		assertEquals(ParentState.EXCEPTION, listener.result);
		
		test.setJobs(1, null);
		
		assertEquals(ParentState.EXCEPTION, listener.result);
		
		test.setJobs(0, null);
		
		assertEquals(ParentState.READY, listener.result);
	}
	
	public void testManyComplete() {
		
		AndState test = new AndState();
		test.setExecutorService(new UnusedServices());
		test.run();
		
		Result listener = new Result();
		test.addStateListener(listener);

		assertEquals(ParentState.READY, listener.result);
		
		FlagState j1 = new FlagState(JobState.COMPLETE);
		FlagState j2 = new FlagState(JobState.COMPLETE);
		FlagState j3 = new FlagState(JobState.COMPLETE);
		FlagState j4 = new FlagState(JobState.COMPLETE);

		j1.run();
		j2.run();
		j3.run();
		j4.run();
		
		test.setJobs(0, j1);
		test.setJobs(1, j2);
		test.setJobs(2, j3);
		test.setJobs(3, j4);
		
		assertEquals(ParentState.COMPLETE, listener.result);
	}
		
	public void testExample() throws ArooaParseException {
		
		DefaultExecutors defaultServices = new DefaultExecutors();
		
		OurDirs dirs = new OurDirs();
		
		File file1 = dirs.relative("oddjob.xml");
		File file2 = dirs.relative("explorer.xml");
		
		Properties properties = new Properties();
		properties.setProperty("file.one", file1.getPath());
		properties.setProperty("file.two", file2.getPath());
		
		FragmentHelper helper = new FragmentHelper();
		helper.setProperties(properties);
		AndState test = (AndState) helper.createComponentFromResource(
				"org/oddjob/state/AndStateExample.xml");
		ArooaSession session = helper.getSession();
		
		ServicesJob.ServiceDefinition def = new ServicesJob.ServiceDefinition();
		def.setService(defaultServices.getPoolExecutor());
		
		ServicesJob services = new ServicesJob();
		services.setRegisteredServices(0, def);
		
		session.getBeanRegistry().register("services", services);
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		test.run();
		
		states.checkNow();
		
		defaultServices.stop();
    }
}
