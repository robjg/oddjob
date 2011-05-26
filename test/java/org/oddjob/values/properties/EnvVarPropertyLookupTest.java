package org.oddjob.values.properties;

import junit.framework.TestCase;

public class EnvVarPropertyLookupTest extends TestCase {

	public void testLookup() {
		
		EnvVarPropertyLookup test = new EnvVarPropertyLookup("env");
		
		String path = test.lookup("env.path");
		
		assertNotNull(path);
		
		path = test.lookup("env.PATH");
		
		assertNotNull(path);
		
		path = test.lookup("env.Path");
		
		assertNotNull(path);
	}
}
