package org.oddjob.swing;

import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.WaitJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.StateSteps;

public class OddjobPanelMain {

	public static void main2(String... args) throws InterruptedException {
		
		DefaultExecutors defaultServices = new DefaultExecutors();
		
		WaitJob job1 = new WaitJob();
		job1.setName("Job One");
		
		FlagState job2 = new FlagState(JobState.COMPLETE);
		job2.setName("Job with Quite a Long Name");
		
		FlagState job3 = new FlagState(JobState.COMPLETE);
		job3.setName("Job Three");

		FlagState job4 = new FlagState(JobState.COMPLETE);
		job4.setName("Job with Another Long Name");
		
		FlagState job5 = new FlagState(JobState.COMPLETE);
		job5.setName("Job Five");
		
		OddjobPanel test = new OddjobPanel();

		test.setJobs(0, job1);
		test.setJobs(1, job2);
		test.setJobs(2, job3);
		test.setJobs(3, job4);
		test.setJobs(4, job5);
		
		test.setExecutorService(defaultServices.getPoolExecutor());
		
		StateSteps states = new StateSteps(test);
		states.setTimeout(0);
		
		states.startCheck(ServiceState.STOPPED, ServiceState.STARTING, 
				ServiceState.STARTED, ServiceState.STOPPED);
		
		test.run();		
		
		states.checkWait();
		
		defaultServices.stop();
	}
	
	public static void main(String... args) throws InterruptedException {
	
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/swing/OddjobPanelExample.xml", 
				OddjobPanelMain.class.getClassLoader()));

		StateSteps states = new StateSteps(oddjob);
		states.setTimeout(Long.MAX_VALUE);
		
		states.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.COMPLETE);
		
		oddjob.run();		
		
		states.checkWait();		
		
		oddjob.destroy();
	}
}
