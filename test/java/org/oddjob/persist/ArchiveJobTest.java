package org.oddjob.persist;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconHelper;
import org.oddjob.jobs.WaitJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.Trigger;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.IconSteps;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Clock;

public class ArchiveJobTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(ArchiveJobTest.class);

    @Before
    public void setUp() throws Exception {

		logger.info("----------------  " + getName() + "  -----------------");
	}
	
   @Test
	public void testWithSimpleJob() throws ComponentPersistException {
		
		final MapPersister persister = new MapPersister();

		FlagState job = new FlagState();
		
		ArchiveJob test = new ArchiveJob();

		test.setArooaSession(new StandardArooaSession());
		test.setArchiver(persister);
		test.setArchiveIdentifier("1");
		test.setArchiveName("test");
		test.setJob(job);

		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		test.run();
		
		states.checkNow();
		
		ComponentPersister cp = persister.persisterFor("test");
		
		Stateful stateful = (Stateful) cp.restore(
				"1", getClass().getClassLoader(), null);
		
		assertEquals(JobState.COMPLETE, stateful.lastStateEvent().getState());
		
		test.destroy();		
	}
	
   @Test
	public void testState() throws FailedToStopException, ComponentPersistException, InterruptedException {

		final MapPersister persister = new MapPersister();

		WaitJob wait = new WaitJob();

		StateSteps waitStates = new StateSteps(wait);
		waitStates.startCheck(JobState.READY, JobState.EXECUTING);
		
		Thread t = new Thread(wait);
		t.start();
		
		waitStates.checkWait();
		
		ArchiveJob test = new ArchiveJob();
		
		test.setArooaSession(new StandardArooaSession());
		test.setArchiver(persister);
		test.setArchiveIdentifier("1");
		test.setArchiveName("test");
		test.setJob(wait);
		
		test.run();

		assertEquals(ParentState.ACTIVE, test.lastStateEvent().getState());
		
		wait.stop();

		t.join();
		
		ComponentPersister cp = persister.persisterFor("test");
		Stateful stateful = (Stateful) cp.restore(
				"1", getClass().getClassLoader(), null);
		
		assertNotNull(stateful);
		
		assertEquals(IconHelper.COMPLETE, OddjobTestHelper.getIconId(stateful));
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, stateful.lastStateEvent().getState());
	}

   @Test
	public void testReflectsChildState() throws FailedToStopException, ComponentPersistException, InterruptedException {

		final MapPersister persister = new MapPersister();

		FlagState job = new FlagState(JobState.INCOMPLETE);

		ArchiveJob test = new ArchiveJob();
		
		test.setArooaSession(new StandardArooaSession());
		test.setArchiver(persister);
		test.setArchiveIdentifier("1");
		test.setArchiveName("test");
		test.setJob(job);
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.INCOMPLETE);
		
		test.run();

		testStates.checkNow();
		
		ComponentPersister cp = persister.persisterFor("test");
		Stateful stateful = (Stateful) cp.restore("1", getClass().getClassLoader(), null);
		
		assertNotNull(stateful);
		
		assertEquals(IconHelper.NOT_COMPLETE, OddjobTestHelper.getIconId(stateful));
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.INCOMPLETE, stateful.lastStateEvent().getState());
	}
	
   @Test
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
		testStates.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		
		test.run();

		testStates.checkWait();

		testStates.startCheck(ParentState.STARTED, 
				ParentState.READY);
		
		test.stop();		
		
		testStates.checkNow();
	}
	
	private static class Always101MillisecondsLater implements Clock {
		
		volatile long millis;
		
		public Always101MillisecondsLater() throws ParseException {
			millis = DateHelper.parseDateTime("2015-04-30 07:45").getTime();
		}
		
		@Override
		public Date getDate() {
			millis = millis + 101;
			return new Date(millis);
		}
	}
	
   @Test
	public void testInOddjob() 
	throws ComponentPersistException, IOException, InterruptedException, ParseException {
		
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
		oddjob.setExport("clock", new ArooaObject(
				new Always101MillisecondsLater()));
		
		oddjob.setArgs(new String[] { baseDir.getPath() });
		
		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, 
				ParentState.COMPLETE);
		
		oddjob.run();
		
		state.checkWait();
		
		oddjob.destroy();

		FilePersister persister = new FilePersister();
		persister.setDir(baseDir);
		
		ComponentPersister thePersister = persister.persisterFor("Batch_01");
		
		String[] archives = thePersister.list();
		
		assertEquals(3, archives.length);
	}
	
   @Test
	public void testStateChangesForAsynchronousJobs() throws InterruptedException {
		
		ArooaSession session = new OddjobSessionFactory().createSession();
		
		DefaultExecutors executors = new DefaultExecutors();
		
		FlagState depends = new FlagState();
		
		FlagState toTrigger = new FlagState();
		
		Trigger trigger = new Trigger();
		trigger.setOn(depends);
		trigger.setJob(toTrigger);
		trigger.setExecutorService(executors.getPoolExecutor());
		
		final MapPersister persister = new MapPersister();

		ArchiveJob test = new ArchiveJob();
		test.setArooaSession(session);
		test.setJob(trigger);
		test.setArchiver(persister);
		test.setArchiveIdentifier("Client_Report");
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.STARTED);
		IconSteps icons = new IconSteps(test);
		icons.startCheck(IconHelper.READY, IconHelper.EXECUTING, IconHelper.STARTED);
		
		test.run();
		
		states.checkNow();
		icons.checkNow();
		
		states.startCheck(ParentState.STARTED, ParentState.ACTIVE, 
				ParentState.COMPLETE);
		icons.startCheck(IconHelper.STARTED, IconHelper.ACTIVE,
				IconHelper.COMPLETE);
		
		depends.run();
		
		states.checkWait();
		icons.checkWait();
		
		executors.stop();
		
		
	}
}
