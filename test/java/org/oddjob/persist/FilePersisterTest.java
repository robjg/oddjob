/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.persist;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.oddjob.Helper;
import org.oddjob.Loadable;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.Resetable;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.SerializableJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;

/**
 *
 * @author Rob Gordon.
 */
public class FilePersisterTest extends TestCase {

	File DIR;
	
	@Override
	protected void setUp() throws Exception {
		OurDirs ourDirs = new OurDirs();
	
		DIR = ourDirs.relative("work/persist");

		if (DIR.exists()) {
			FileUtils.forceDelete(DIR);
		}
		FileUtils.forceMkdir(DIR);
	}
	
	public static class OurJob extends SerializableJob {
		private static final long serialVersionUID = 2008110500;
		
		private String name;
		
		private String text;
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setText(String text) {
			this.text = text;
		}
		
		@Override
		protected int execute() throws Throwable {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	/** 
	 * Simple test of persisting something
	 * @throws ComponentPersistException 
	 *
	 */
	public void testPersistAndLoad() throws ComponentPersistException {
		OurJob job = new OurJob();
		job.setName("Test");
		job.setText("Hello World");
		job.run();

		StandardArooaSession session = new StandardArooaSession();
		
		FilePersister test = new FilePersister();
		test.setDir(DIR);

		ComponentPersister persister = test.persisterFor(null);

		persister.persist("test-persist", job, session);
		
		File check = new File(DIR, "test-persist.ser");
		assertTrue(check.exists());
			
		job = (OurJob) persister.restore("test-persist", 
				getClass().getClassLoader(), session);
		
		assertEquals("Test", job.name);
		assertEquals("Hello World", job.text);
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(job));

		// check we can run it again.
		
		((Resetable) job).hardReset();
		
		assertEquals(JobState.READY, Helper.getJobState(job));
		
		job.run();
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(job));
	}

	class OurSession extends MockArooaSession {
		
		@Override
		public ComponentPool getComponentPool() {
			return new MockComponentPool() {
				
			};
		}
	}
	
	public void testFailsOnNoDirectory() {
		
		FilePersister test = new FilePersister();
		
		test.setDir(new File(DIR, "idontexist"));
		
		try {
			test.persist((Path) null, (String) null, (Object) null);
			fail();
		}
		catch (ComponentPersistException e) {
			assertTrue(e.getMessage().startsWith("No directory"));
		}
	}
	
	public void testCreatesFullPath() throws ComponentPersistException {
		
		FilePersister test = new FilePersister();
		
		test.setDir(DIR);
		
		test.persist(new Path("a/b/c"), "x", new OurJob());
		
		File check = new File(DIR, "a/b/c/x.ser");
		
		assertTrue(check.exists());
	}
	
	public void testNullDirectory() throws ComponentPersistException {
		FilePersister persister = new FilePersister();
		
		try {
			persister.directoryFor(new Path());
			fail("No directory should fail.");
		}
		catch (NullPointerException e) {
			// expected
		}
	}
	
	public void testPersistExample() throws ArooaPropertyException, ArooaConversionException, URISyntaxException {
		
    	URL url = getClass().getClassLoader().getResource("org/oddjob/persist/FilePersisterExample.xml");
    	
    	File file = new File(url.toURI().getPath());
    	        
		Properties props = new Properties();
		props.setProperty("important.stuff", "Important Stuff!");
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setFile(file);
		oddjob1.setArgs(new String[] { DIR.getAbsolutePath() });
		oddjob1.setProperties(props);
		oddjob1.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob1.lastStateEvent().getState());
		oddjob1.destroy();
		
		assertTrue(new File(DIR, "important-jobs/save-me.ser").exists());
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setFile(file);
		oddjob2.setArgs(new String[] { DIR.getAbsolutePath() });
		oddjob2.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob2);
		
		Loadable loadable = lookup.lookup("important-jobs", Loadable.class);
		loadable.load();
		
		String text = lookup.lookup("important-jobs/save-me.text", 
				String.class);
		
		assertEquals("Important Stuff!", text);
		
		oddjob2.destroy();		
	}
	
}
