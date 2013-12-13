package org.oddjob.oddballs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.tools.OurDirs;

public class OddballDescriptorFactoryTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		
		new BuildOddballs().run();
	}
	
	public void testCreate() throws URISyntaxException {
		
		OurDirs dirs = new OurDirs();
		
		OddballsDescriptorFactory test = new OddballsDescriptorFactory();
		test.setFiles(new File[] {
				new File(dirs.base(), "test/oddballs/apple") });
		
		ArooaDescriptor descriptor = test.createDescriptor(getClass().getClassLoader());
		
		InstantiationContext instantiationContext = 
			new InstantiationContext(ArooaType.COMPONENT, null);
		
		ArooaClass appleClass = 
			descriptor.getElementMappings().mappingFor(
				new ArooaElement(new URI("http://rgordon.co.uk/fruit"), 
						"apple"), instantiationContext);
		
		assertEquals("fruit.Apple", ((SimpleArooaClass) 
				appleClass).forClass().getName());
	}
}
