package org.oddjob.values.properties;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class EnvVarPropertyLookupTest extends OjTestCase {

   @Test
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
