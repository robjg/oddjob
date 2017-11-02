package org.oddjob;

import org.junit.Test;

import java.io.File;

import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.ListDescriptorBean;
import org.oddjob.oddballs.OddballsDescriptorFactory;
import org.oddjob.oddballs.OddballsDirDescriptorFactory;

import org.oddjob.OjTestCase;

public class OddjobBuilderTest extends OjTestCase {

   @Test
	public void testResolveOddballs() {
		
		OddjobBuilder test = new OddjobBuilder();
		
		ArooaDescriptorFactory result;
		
		test.setNoOddballs(false);
		test.setOddballsDir(null);
		test.setOddballsPath(null);
		
		result = test.resolveOddballs();
		
		assertEquals(OddballsDirDescriptorFactory.class, result.getClass());
				
		test.setNoOddballs(true);
		test.setOddballsDir(null);
		test.setOddballsPath(null);
		
		result = test.resolveOddballs();
		
		assertEquals(null, result);
		
		test.setNoOddballs(true);
		test.setOddballsDir(null);
		test.setOddballsPath("my-oddball");
		
		result = test.resolveOddballs();

		assertEquals(OddballsDescriptorFactory.class, result.getClass());
		
		test.setNoOddballs(true);
		test.setOddballsDir(new File("oddball-dir"));
		test.setOddballsPath(null);
		
		result = test.resolveOddballs();

		assertEquals(OddballsDirDescriptorFactory.class, result.getClass());
		
		test.setNoOddballs(false);
		test.setOddballsDir(new File("oddball-dir"));
		test.setOddballsPath("my-oddball");
		
		result = test.resolveOddballs();

		assertEquals(ListDescriptorBean.class, result.getClass());
	}
}
