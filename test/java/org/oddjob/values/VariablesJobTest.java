/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.values;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.ElementMappings;
import org.oddjob.arooa.beandocs.MappingsContents;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;

/**
 * Test for Variables Job.
 * 
 * @author Rob Gordon.
 */
public class VariablesJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(VariablesJobTest.class);
	protected void setUp() {
		logger.debug("-------------- " + getName() + " -------------");
	}
	
	// test set and get
	public void testSimple() throws Exception {
		ValueType vt = new ValueType();
		vt.setValue(new ArooaObject("fred"));
		
		VariablesJob test = new VariablesJob();
		
		PropertyUtils.setProperty(test, "test", vt);
		//try to cause concurrent modification exception on reset.
		PropertyUtils.setProperty(test, "another", vt); 
		
		ArooaValue result = (ArooaValue) PropertyUtils.getProperty(test, "test");
		
		assertNotNull(result);
		
		Map<String, String> description = new UniversalDescriber(
				new StandardArooaSession()).describe(test);
		assertTrue(description.containsKey("test"));
		
    	ArooaDescriptor descriptor = new OddjobDescriptorFactory(
			).createDescriptor(null);
    	
		ArooaSession session = new StandardArooaSession(
				descriptor);
		
		ArooaConverter converter = session.getTools().getArooaConverter();
		
		assertEquals("fred", converter.convert(result, Object.class));
		
		assertTrue(test.hardReset());
		
		assertNull(PropertyUtils.getProperty(test, "test"));
		
		description = new UniversalDescriber(
				new StandardArooaSession()).describe(test);
		assertFalse(description.containsKey("test"));
	}
	
	/**
	 *  test the type property when setting a variables job.
	 */
	public void testTypesForSetting() {
		
		ArooaDescriptor descriptor = new VariablesJobDescriptorFactory(
				).createDescriptor(null);

		InstantiationContext instantiationContext = 
			new InstantiationContext(ArooaType.COMPONENT, null);
		
		ArooaClass arooaClass = descriptor.getElementMappings(
				).mappingFor(new ArooaElement("variables"), 
						instantiationContext);
		
		VariablesJob vj = (VariablesJob) arooaClass.newInstance();
		
		vj.set("fruit", new ValueType());

		PropertyAccessor propertyAccessor = new BeanUtilsPropertyAccessor();

		BeanOverview beanOverview = arooaClass.getBeanOverview(
				propertyAccessor);
		
		// set or unset - they're all ArooaValues.
		assertEquals(ArooaValue.class, 
				beanOverview.getPropertyType("fruit"));
		assertEquals(ArooaValue.class, 
				beanOverview.getPropertyType("vegtables"));		
	}	
	
	public static class SessionCapture implements ArooaSessionAware {
	
		ArooaSession arooaSession;
		
		public void setArooaSession(ArooaSession session) {
			this.arooaSession = session;
		}
		
		public ArooaSession getArooaSession() {
			return arooaSession;
		}
	}
	
	public void testArooaDescriptor() throws ArooaConversionException {

		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='sc' class='" + SessionCapture.class.getName() + "'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();
		
		ArooaSession session = new OddjobLookup(
				oddjob).lookup("sc.arooaSession", ArooaSession.class);

		InstantiationContext instantiationContext = 
			new InstantiationContext(ArooaType.COMPONENT, null);
		
		ArooaClass classId = session.getArooaDescriptor().getElementMappings(
				).mappingFor(
						VariablesJobDescriptorFactory.VARIABLES, 
						instantiationContext);
		
		// Maybe these should be the same.
//		ArooaClass classId session.getTools().getPropertyAccessor().getClassName(
//				new VariablesJob());
		
		ArooaBeanDescriptor beanDescriptor = 
			session.getArooaDescriptor().getBeanDescriptor(classId,
					session.getTools().getPropertyAccessor());

		assertEquals(ConfiguredHow.ELEMENT, 
				beanDescriptor.getConfiguredHow("apples"));
		
		assertEquals(ConfiguredHow.ATTRIBUTE, 
				beanDescriptor.getConfiguredHow("id"));
		
		Object instance = classId.newInstance();
		
		assertEquals(VariablesJob.class, instance.getClass());
	}
	
	public void testDescriptorBeanDoc() {
		
		VariablesJobDescriptorFactory test = 
			new VariablesJobDescriptorFactory();
		
		ArooaDescriptor descriptor = test.createDescriptor(
				getClass().getClassLoader());
		
		ElementMappings mappings = descriptor.getElementMappings();
		
		MappingsContents contents = mappings.getBeanDoc(ArooaType.COMPONENT);
		
		ArooaElement[] elements  = contents.allElements();
		assertEquals(1, elements.length);
		
		assertEquals(VariablesJobDescriptorFactory.VARIABLES,
				elements[0]);
		
		ArooaClass arooaClass = contents.documentClass(elements[0]);
		
		BeanOverview overview = arooaClass.getBeanOverview(new BeanUtilsPropertyAccessor());

		String[] properties = overview.getProperties();
		
		assertEquals(0, properties.length);
	}
	
	/**
	 * Check getting the values back with bean methods.
	 */
	public void testGetValues() throws Exception {
		PropertyAccessor propertyAccessor = new BeanUtilsPropertyAccessor();
		
		// create 
		SimpleBeanRegistry cr = new SimpleBeanRegistry();

		VariablesJob j = new VariablesJob();
		cr.register("myj", j);
		
		propertyAccessor.setSimpleProperty(j, "test", new Boolean(true));		
		propertyAccessor.setSimpleProperty(j, "next", new Short((short) 1234));
		
		Object o1 = cr.lookup("myj.test");
		Object r1 = new DefaultConverter().convert(
				o1, Boolean.TYPE);
		assertEquals(Boolean.class, r1.getClass());
		assertEquals(new Boolean(true), r1);

		Object o2 = cr.lookup("myj.next");
		Object r2 = new DefaultConverter().convert(
				o2, Short.TYPE);
		assertEquals(Short.class, r2.getClass());
		assertEquals(new Short((short) 1234), r2);
	}
	
	public void testNullValue() throws Exception {
		String xml=
			"<oddjob>" +
			" <job>" +
			"  <variables id='v'>" +
			"    <rubbish>" +
			"      <value value='${nosuchid}'/>" +
			"    </rubbish>" +
			"  </variables>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		oj.run();
		
		DynaBean b = (DynaBean) new OddjobLookup(oj).lookup("v");
		assertNotNull(b);		
		
		ValueType result = (ValueType) b.get("rubbish");
		
		assertNotNull(result);
		assertNull(result.getValue());
				
	}

	public void testSelfUse() throws Exception {
			
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <variables id='v'>" +
			"   <today>" +
			"  	 <date date='2005-12-25' timeZone='GMT'/>" +
			"   </today>" +
			"   <yyyymmdd_today>" +
			"    <format date='${v.today}' format='yyyyMMdd'/>" +
			"   </yyyymmdd_today>" +
			"  </variables>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		assertEquals("20051225", 
				new OddjobLookup(oj).lookup(
						"v.yyyymmdd_today", String.class));
		
		oj.destroy();
	}

	
    public void testInOddjob() {
        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("Resource",
        		getClass().getResourceAsStream("variables-test.xml")));
        oj.run();
        
        CheckBasicSetters check = (CheckBasicSetters) new OddjobLookup(
        		oj).lookup("check");
        
        assertEquals("Job state", JobState.COMPLETE, OddjobTestHelper.getJobState(check));
    }
    
    public void testExample() {
    	
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/values/VariablesExample.xml",
    			getClass().getClassLoader()));
    	
    	ConsoleCapture console = new ConsoleCapture();
    	try (ConsoleCapture.Close close = console.captureConsole()) {
        	
        	oddjob.run();
    	}
    	
    	console.dump(logger);
    	
    	String[] lines = console.getLines();
    	
    	assertEquals("Hello World", lines[0].trim());    	
    	assertEquals(1, lines.length);
    	
    	oddjob.destroy();
    }
}
