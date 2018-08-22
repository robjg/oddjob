package org.oddjob;

import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.ElementMappings;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionPath;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.deploy.ClassPathDescriptorFactory;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.jobs.SequenceJob;
import org.oddjob.jobs.structural.JobFolder;

public class OddjobArooaDescriptorTest extends OjTestCase {

   @Test
	public void testArooaXml() {
		
		URL url = getClass().getClassLoader().getResource(
				ClassPathDescriptorFactory.AROOA_FILE);
		
		assertNotNull(url);

		ClassPathDescriptorFactory factory = 
			new ClassPathDescriptorFactory();
				
		ArooaDescriptor test = factory.createDescriptor(
				getClass().getClassLoader());
		
		ElementMappings definitions = 
			test.getElementMappings();
		
		ArooaElement[] elements = definitions.elementsFor(
				new InstantiationContext(ArooaType.COMPONENT, null));

		assertTrue(elements.length > 10);
		

	}
		
	
	
   @Test
	public void testLoad() throws ArooaParseException, ClassNotFoundException {
		
    	ArooaDescriptor test = new OddjobDescriptorFactory(
			).createDescriptor(null);
		
		ElementMappings mappings = 
			test.getElementMappings();
		
		ArooaElement[] elements = mappings.elementsFor(
				new InstantiationContext(ArooaType.COMPONENT, null));

		assertTrue(elements.length > 10);
			
		assertEquals(new SimpleArooaClass(SequenceJob.class), mappings.mappingFor(
				new ArooaElement("sequence"), 
				new InstantiationContext(ArooaType.COMPONENT, null)));
		
		elements = mappings.elementsFor(
				new InstantiationContext(ArooaType.VALUE, null));

		assertTrue(elements.length > 10);
				
		assertNotNull(test.getElementMappings().mappingFor(
				new ArooaElement("oddjob"), 
				new InstantiationContext(ArooaType.COMPONENT, null)));
	}
	
	/**
	 * Tracking down some weird features of the Designer
	 * @throws ArooaParseException 
	 */
   @Test
	public void testSupports() throws Exception {

		ArooaSession session = new OddjobSessionFactory().createSession();
		
		ArooaElement[] elements = 
			session.getArooaDescriptor().getElementMappings().elementsFor(
							new InstantiationContext(ArooaType.VALUE, 
									new SimpleArooaClass(Object.class), 
									new DefaultConverter())
									);
		
		Set<ArooaElement> set = new HashSet<ArooaElement>();
		set.addAll(Arrays.asList(elements));
		
		assertTrue(set.contains(
				new ArooaElement(
						new URI("http://rgordon.co.uk/oddjob/arooa"), 
						"bean-def")));

		assertTrue(set.contains(ValueType.ELEMENT));
	}

   @Test
	public void testSupportsArooaValue() throws Exception {

		ArooaSession session = new OddjobSessionFactory().createSession();
		
		ArooaElement[] elements = 
			session.getArooaDescriptor().getElementMappings().elementsFor(
						new InstantiationContext(ArooaType.VALUE,
								new SimpleArooaClass(ArooaValue.class)));
		
		Set<ArooaElement> set = new HashSet<ArooaElement>();
		set.addAll(Arrays.asList(elements));
		
		assertTrue(set.contains(
				new ArooaElement( 
						"file")));

		assertTrue(set.contains(ValueType.ELEMENT));
	}

   @Test
	public void testArooaBeanDescriptor() {
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
    	ArooaSession session = new StandardArooaSession(descriptor);

    	ArooaClass arooaClass = new SimpleArooaClass(JobFolder.class);
    	
    	ArooaBeanDescriptor beanDescriptor = session.getArooaDescriptor(
    			).getBeanDescriptor(arooaClass, 
    					session.getTools().getPropertyAccessor());

    	assertEquals("jobs", beanDescriptor.getComponentProperty());
	}
	
	/** Finding a bug with setting properties. */
   @Test
	public void testSomeConversions() {
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
    	ArooaSession session = new StandardArooaSession(descriptor);

    	ArooaConverter converter = session.getTools().getArooaConverter();
    	
    	ConversionPath<Long, ArooaValue> path = 
    		converter.findConversion(Long.class, ArooaValue.class);
    	
    	assertEquals("Long-Number-Object-ArooaValue", path.toString());
	}
}
