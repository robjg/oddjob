package org.oddjob.beanbus.mega;

import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.Describeable;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.beanbus.AbstractDestination;

public class CollectionProxyGeneratorTest extends TestCase {

	public class OurDestination extends AbstractDestination<String> {
		
		public boolean add(String e) {
			return false;
		}
	}
	
	public void testCreate() {
				
		CollectionProxyGenerator test = new CollectionProxyGenerator();

		Object proxy = test.generate(
				new OurDestination(), getClass().getClassLoader());
		
		((ArooaSessionAware) proxy).setArooaSession(new StandardArooaSession());
		
		assertTrue(proxy instanceof Describeable);
		
		Map<String, String> description = ((Describeable) proxy).describe();
		
		assertNotNull(description);
	}
}
