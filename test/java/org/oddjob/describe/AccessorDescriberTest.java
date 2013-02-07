package org.oddjob.describe;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaMap;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.beanutils.MagicBeanDefinition;
import org.oddjob.arooa.beanutils.MagicBeanDescriptorProperty;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.WrapDynaBean;

public class AccessorDescriberTest extends TestCase {
	
	public static class SimpleBean {
		public String getFruit() {
			return "apples";
		}
		
		@NoDescribe
		public String getColour() {
			return "red";
		}
	}
	
	public void testSimpleBean() {
		
		ArooaSession session = new StandardArooaSession();
		
		Describer test = new AccessorDescriber(session);
		
		Map<String, String> description = test.describe(new SimpleBean());
		
		assertEquals(2, description.size());
		assertEquals("apples", description.get("fruit"));
	}

	public static class SetterOnlyBean {
		public void setFruit(String fruit) {
		}
	}	
	
	public void testDynaBean() {
		
		ArooaSession session = new StandardArooaSession();
		
		WrapDynaBean wrap = new WrapDynaBean(new SetterOnlyBean());
		
		Describer test = new AccessorDescriber(session);
		
		Map<String, String> description = test.describe(wrap);
		
		assertEquals(1, description.size());
		assertEquals(SetterOnlyBean.class.toString(), 
				description.get("class"));
	}
		
	/**
	 * Test with capital letter property. 
	 *
	 */
	public void testCapitalPropertiesDynaBean() {
		
		ArooaSession session = new StandardArooaSession();
		
		LazyDynaMap bean = new LazyDynaMap();
		
		bean.set("Fruit", "apple");
		
		Describer test = new AccessorDescriber(session);
		
		Map<String, String> description = test.describe(bean);
		
		assertEquals(1, description.size());
		assertEquals("apple", description.get("Fruit"));
		
	}
	
	/**
	 * Test magic bean - tracking down a bug in JMXAttributes. 
	 *
	 */
	public void testMagicDynaBean() {
		
		ArooaSession session = new StandardArooaSession();
		
		MagicBeanDefinition def = new MagicBeanDefinition();
		def.setElement("Foo");
		
		MagicBeanDescriptorProperty prop0 = new MagicBeanDescriptorProperty();
		prop0.setName("Fruit");
		prop0.setType(String.class.getName());
		
		def.setProperties(0, prop0);
		
		ArooaClass arooaClass = def.createMagic(getClass().getClassLoader());
		DynaBean bean = (DynaBean) arooaClass.newInstance();
		
		bean.set("Fruit", "apple");
		
		Describer test = new AccessorDescriber(session);
		
		Map<String, String> description = test.describe(bean);
		
		assertEquals(1, description.size());
		assertEquals("apple", description.get("Fruit"));
		
	}
}
