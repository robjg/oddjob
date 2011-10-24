package org.oddjob.io;

import org.oddjob.FragmentHelper;
import org.oddjob.arooa.ArooaParseException;

import junit.framework.TestCase;

public class StdinTypeTest extends TestCase {

	public void testExample() throws ArooaParseException {
		
		// Just test XML for now.

		FragmentHelper helper = new FragmentHelper();
		helper.createComponentFromResource(
				"org/oddjob/io/StdinTypeExample.xml");
		
	}
}
