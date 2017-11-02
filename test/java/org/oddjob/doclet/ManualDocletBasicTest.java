package org.oddjob.doclet;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.oddballs.BuildOddballs;
import org.oddjob.tools.OurDirs;

public class ManualDocletBasicTest extends OjTestCase {

	OurDirs dirs = new OurDirs();
	
   @Test
	public void testJobsAndTypes() {
		
		new BuildOddballs().run();
		
		String descriptorPath = new FileConvertlets().filesToPath(
				new File[] { dirs.relative("test/oddballs/apple/classes"),
						dirs.relative("test/oddballs/orange/classes")});
		
		ManualDoclet test = new ManualDoclet(descriptorPath, null);
		
		JobsAndTypes jats = test.jobsAndTypes();
		
		List<String> types = new ArrayList<String>();
		for (String type : jats.types()) {
			types.add(type);
		}
		
		assertEquals(2, types.size());
		
		assertTrue(types.contains("fruit:colour"));
		assertTrue(types.contains("fruit:flavour"));
		
		List<String> jobs = new ArrayList<String>();
		for (String type : jats.jobs()) {
			jobs.add(type);
		}
		
		assertEquals(2, jobs.size());
		
		assertTrue(jobs.contains("fruit:apple"));
		assertTrue(jobs.contains("fruit:orange"));
	}
}
