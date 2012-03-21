package org.oddjob.io;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.oddjob.FragmentHelper;
import org.oddjob.arooa.ArooaParseException;

import junit.framework.TestCase;

public class StdinTypeTest extends TestCase {

	public void testExample() throws ArooaParseException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		// Just test XML for now.

		FragmentHelper helper = new FragmentHelper();
		Object copy = helper.createComponentFromResource(
				"org/oddjob/io/StdinTypeExample.xml");
				
		assertEquals(new File("foo.txt"), 
				PropertyUtils.getProperty(copy, "to"));
	}
}
