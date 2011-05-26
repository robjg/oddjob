/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import org.oddjob.arooa.registry.SimpleBeanRegistry;

import junit.framework.TestCase;

public class JobTokenTest extends TestCase {

	public void test1() {
		Object job = new Object();
		JobToken jobToken = JobToken.create(null, job);
		assertEquals(job.toString(), jobToken.toString());
	}
	
	public void test2() {
		Object job = new Object();
		
		SimpleBeanRegistry cr = new SimpleBeanRegistry();
		cr.register("x", job);
		
		JobToken jobToken = JobToken.create(cr, job);
		assertEquals("Path: x", jobToken.toString());
	}

	public void testNoPath() {
		Object job = new Object();
		
		SimpleBeanRegistry cr = new SimpleBeanRegistry();
		
		try {
			JobToken.create(cr, job);
			fail("No path should be detected.");
		} catch (NullPointerException e) {
			// expected
		}
	}
}
