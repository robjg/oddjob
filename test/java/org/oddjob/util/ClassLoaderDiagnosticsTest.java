package org.oddjob.util;

import junit.framework.TestCase;

import org.oddjob.Oddjob;

public class ClassLoaderDiagnosticsTest extends TestCase {

	public void testResource() {
		
		ClassLoaderDiagnostics test = new ClassLoaderDiagnostics();
		
		test.setResource("META-INF/arooa.xml");
		
		test.run();
		
		assertTrue(test.getLocation().contains("arooa.xml"));
	}
	
	public void testClassName() {
		
		ClassLoaderDiagnostics test = new ClassLoaderDiagnostics();
		
		test.setClassName(Oddjob.class.getName());
		
		test.run();
		
		assertTrue(test.getLocation().contains("Oddjob.class"));
	}
}
