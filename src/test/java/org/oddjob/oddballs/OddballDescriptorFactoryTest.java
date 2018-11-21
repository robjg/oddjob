package org.oddjob.oddballs;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.OurDirs;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class OddballDescriptorFactoryTest extends OjTestCase {

    @Before
    public void setUp() throws Exception {
		
		new BuildOddballs().run();
	}
	
   @Test
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
