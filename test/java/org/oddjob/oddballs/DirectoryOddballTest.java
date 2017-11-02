package org.oddjob.oddballs;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ElementMappings;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.tools.OurDirs;

public class DirectoryOddballTest extends OjTestCase {

    @Before
    public void setUp() throws Exception {
		
		new BuildOddballs().run();
	}

   @Test
	public void testCreate() throws URISyntaxException, ClassNotFoundException {
		
		DirectoryOddball test = new DirectoryOddball();
		
		OurDirs dirs = new OurDirs();
		
		Oddball result = test.createFrom(
				new File(dirs.base(), 
						"test/oddballs/apple"), getClass().getClassLoader());
		
		ArooaDescriptor descriptor = result.getArooaDescriptor();
		
		assertNotNull(descriptor);
		
		InstantiationContext instantiationContext = 
			new InstantiationContext(ArooaType.COMPONENT, null);
		
		ElementMappings mappings = descriptor.getElementMappings();
		assertNotNull(mappings);
		
		ArooaClass appleClass = 
			mappings.mappingFor(
				new ArooaElement(new URI("http://rgordon.co.uk/fruit"), 
						"apple"), instantiationContext);
		
		assertEquals("fruit.Apple", ((SimpleArooaClass) 
				appleClass).forClass().getName());
		
		ClassLoader loader = result.getClassLoader();
		
		assertNotNull(loader.loadClass("fruit.Apple"));
	}
}
