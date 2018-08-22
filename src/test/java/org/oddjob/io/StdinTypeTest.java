package org.oddjob.io;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.tools.FragmentHelper;

import org.oddjob.OjTestCase;

public class StdinTypeTest extends OjTestCase {

   @Test
	public void testExample() throws ArooaParseException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		// Just test XML for now.

		FragmentHelper helper = new FragmentHelper();
		Object copy = helper.createComponentFromResource(
				"org/oddjob/io/StdinTypeExample.xml");
				
		assertEquals(new File("foo.txt"), 
				PropertyUtils.getProperty(copy, "to"));
	}
}
