package org.oddjob.persist;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OurDirs;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconHelper;
import org.oddjob.jobs.WaitJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.Trigger;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;

public class ArchiveJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ArchiveJobTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.info("----------------  " + getName() + "  -----------------");
	}
	
	public void testState() throws FailedToStopException, ComponentPersistException, InterruptedException {

		final MapPersister persister = new MapPersister();

		WaitJob wait = new WaitJob();

		StateSteps states = new StateSteps(wait);
		states.startCheck(JobState.READY, JobState.EXECUTING);
		
		Thread t = new Thread(wait);
		t.start();
		
		states.checkWait();
		
		ArchiveJob test = new ArchiveJob();
		test.setArchiver(persister);
		test.setArchiveIdentifier("1");
		test.setArchiveName("test");
		test.setJob(wait);
		
		test.run();

		assertEquals(JobState.EXECUTING, test.lastStateEvent().getState());
		
		wait.stop();

		t.join();
		
		ComponentPersister cp = persister.persisterFor("test");
		Stateful stateful = (Stateful) cp.restore(
				"1", getClass().getClassLoader(), null);
		
		assertNotNull(stateful);
		
		assertEquals(IconHelper.COMPLETE, Helper.getIconId(stateful));
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, stateful.lastStateEvent().getState());
	}

	public void testReflectsChildState() throws FailedToStopException, ComponentPersistException, InterruptedException {

		final MapPersister persister = new MapPersister();

		FlagState job = new FlagState(JobState.INCOMPLETE);

		ArchiveJob test = new ArchiveJob();
		test.setArchiver(persister);
		test.setArchiveIdentifier("1");
		test.setArchiveName("test");
		test.setJob(job);
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(JobState.READY, 
				JobState.EXECUTING, JobState.INCOMPLETE);
		
		test.run();

		testStates.checkNow();
		
		ComponentPersister cp = persister.persisterFor("test");
		Stateful stateful = (Stateful) cp.restore("1", getClass().getClassLoader(), null);
		
		assertNotNull(stateful);
		
		assertEquals(IconHelper.NOT_COMPLETE, Helper.getIconId(stateful));
		assertEquals(JobState.INCOMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.INCOMPLETE, stateful.lastStateEvent().getState());
	}
	
	public void testStop() throws InterruptedException, FailedToStopException {
		
		DefaultExecutors executors = new DefaultExecutors();
		
		final MapPersister persister = new MapPersister();

		FlagState depends = new FlagState();
		FlagState neverRuns = new FlagState();
		
		Trigger trigger = new Trigger();
		trigger.setExecutorService(executors.getPoolExecutor());
		trigger.setOn(depends);
		trigger.setJob(neverRuns);
		
		ArchiveJob test = new ArchiveJob();
		test.setArchiver(persister);
		test.setArchiveIdentifier("1");
		test.setArchiveName("test");

		test.setJob(trigger);

		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(JobState.READY, 
				JobState.EXECUTING);
		
		test.run();

		testStates.checkWait();

		testStates.startCheck(JobState.EXECUTING, 
				JobState.READY);
		
		test.stop();		
		
		testStates.checkNow();
	}
	
	public void testInOddjob() 
	throws ComponentPersistException, IOException, InterruptedException {
		
		OurDirs dirs = new OurDirs();
		
		File baseDir = dirs.relative("work/archiver");
		
		if (baseDir.exists()) {
			FileUtils.forceDelete(baseDir);
		}
		FileUtils.forceMkdir(baseDir);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/persist/ArchiveJobTest.xml",
				getClass().getClassLoader()));
		oddjob.setArgs(new String[] { baseDir.getPath() });
		
		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.COMPLETE);
		
		oddjob.run();
		
		state.checkWait();
		
		oddjob.destroy();

		FilePersister persister = new FilePersister();
		persister.setDir(baseDir);
		
		ComponentPersister thePersister = persister.persisterFor("Batch_01");
		
		String[] archives = thePersister.list();
		
		assertEquals(3, archives.length);
	}
}
