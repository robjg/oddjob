/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.types;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.oddjob.ConverterHelper;
import org.oddjob.OddjobTestHelper;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.standard.StandardArooaSession;

/**
 * Test PropertyType
 */
public class PropertyTypeTest extends TestCase {
	
	/** 
	 * Simple test to check valueFor
	 */
	public void testValueForString() throws Exception {
		
		PropertyType test = new PropertyType();
		test.set("x", "Test");

		Object result = test.get("x");
		
		assertNotNull(result);
		assertEquals(PropertyType.class, result.getClass());
				
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		assertEquals("Test", 
				converter.convert(result, String.class));
	}
	

	/**
	 * Test that when a null property is added a
	 * PropertyType with a null value is created.
	 * 
	 * @throws Exception
	 */
	public void testAddingNull() throws Exception {
		
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		PropertyType p = new PropertyType();
		
		PropertyUtils.setProperty(p, "aaa.bbb.1", null);
		
		String value = converter.convert(
				PropertyUtils.getProperty(p, "aaa.bbb.1"), String.class);

		assertEquals(null, value);
		
		value = converter.convert(
				PropertyUtils.getProperty(p, "aaa.bbb.2"), String.class);

		assertEquals(null, value);

		Properties props = p.toProperties();
		
		assertEquals(0, props.size());
	}
	
	
	/**
	 * At the moment PropertyType would have to create a
	 * null type for every get which didn't exist if this
	 * sort of thing were going to work - maybe it should.
	 * 
	 * @throws Exception
	 */
	public void testAddingNestedPropertyFails() throws Exception {
		PropertyType p = new PropertyType();

		PropertyUtils.setProperty(p, "aaa.bbb.1", "foo");

		PropertyType result = (PropertyType)
				PropertyUtils.getProperty(p, "aaa.bbb.1");

		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		assertEquals("foo", converter.convert(result, String.class));
	}
	
	/**
	 * Test setting properties (on a node that already
	 * exists).
	 * 
	 * @throws Exception
	 */
	public void testSettingProperties() throws Exception {
		PropertyType p = new PropertyType();

		PropertyUtils.setProperty(p, "aaa.bbb.1", null);
		
		PropertyUtils.setProperty(p, "aaa.bbb.1", "foo");
		PropertyUtils.setProperty(p, "aaa.bbb.2", "ba");
							
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		assertEquals("foo", converter.convert(
				PropertyUtils.getProperty(p, "aaa.bbb.1"), String.class));
		assertEquals("ba", converter.convert(
				PropertyUtils.getProperty(p, "aaa.bbb.2"), String.class));
	}

	/** 
	 * Check properties are returned.
	 */
	public void testGetProperties() throws Exception {
		PropertyType p = new PropertyType();

		PropertyUtils.setProperty(p, "aaa.bbb", null);

		PropertyUtils.setProperty(p, "aaa.bbb.1", "foo");
		PropertyUtils.setProperty(p, "aaa.bbb.2", "ba");
					
		Properties result = new Properties();
		p.properties(result, "");
		
		assertEquals("foo", result.get("aaa.bbb.1"));
		assertEquals("ba", result.get("aaa.bbb.2"));
	}
	
	public void getPropertyType() throws Exception {
		
		PropertyType test = new PropertyType();
		
		Class<?> type = PropertyUtils.getPropertyType(test, "thing");
		
		assertEquals(String.class, type);
	}
	
	public void testUsingBeanUtilsBeanHelper() throws Exception {
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
    	
		ArooaSession session = new StandardArooaSession(
				descriptor);
		
		ArooaConverter converter = 
			session.getTools().getArooaConverter();
		
		PropertyType test = new PropertyType();
		
		PropertyAccessor accessor = session.getTools().getPropertyAccessor();
		
		accessor.setProperty(test, "snack.fruit.favourite.1", "apples");
		PropertyUtils.setProperty(test, "snack.fruit.favourite.2", "oranges");
				
		PropertyType result = (PropertyType) accessor.getProperty(
				test, "snack.fruit");
		
		Properties props = result.toProperties();
		assertEquals("apples", props.getProperty("fruit.favourite.1"));
		assertEquals("oranges", props.getProperty("fruit.favourite.2"));
		
		PropertyType result2 = (PropertyType) accessor.getProperty(
				result, "favourite.1");
		
		assertEquals("apples", converter.convert(result2, String.class));
	}
	
	public void testSerialize() throws Exception {
		
		PropertyType p = new PropertyType();

		PropertyUtils.setProperty(p, "snack.fruit.favourite.1", "apples");
		PropertyUtils.setProperty(p, "snack.fruit.favourite.2", "oranges");
		
		PropertyType copy = OddjobTestHelper.copy(p);
		
		Properties results = copy.toProperties();
		
		assertEquals("apples", results.getProperty("snack.fruit.favourite.1"));
		assertEquals("oranges", results.getProperty("snack.fruit.favourite.2"));
	}
}
