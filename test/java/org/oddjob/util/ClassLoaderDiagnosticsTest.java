package org.oddjob.util;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;

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
	
	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(
				new XMLConfiguration("org/oddjob/util/ClassLoaderDiagnostics.xml", 
				getClass().getClassLoader()));

		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		String loc1 = lookup.lookup("for-class.location", String.class);
		
		String loc2 = lookup.lookup("for-resource.location", String.class);
		
		assertNotNull(loc1);
		assertNotNull(loc2);
	}
}
