package org.oddjob.doclet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.OurDirs;
import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.oddballs.BuildOddballs;

public class ManualDocletBasicTest extends TestCase {

	OurDirs dirs = new OurDirs();
	
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
