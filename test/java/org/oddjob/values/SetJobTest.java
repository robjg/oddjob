/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.values;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.oddjob.OddjobTestHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.MockArooaBeanDescriptor;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.deploy.NoAnnotations;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

/**
 *
 * @author Rob Gordon.
 */
public class SetJobTest extends TestCase {

	public static class SimpleBean {
		String prop;
		
		public void setProp(String prop) {
			this.prop = prop;
		}
	}
	
	private class OurSession extends MockArooaSession {
		Object bean;

		@Override
		public BeanRegistry getBeanRegistry() {
			return new MockBeanRegistry() {
				@Override
				public Object lookup(String path) {
					assertEquals("obj", path);
					return bean;
				}
				
			};
		}
		
		@Override
		public ComponentPool getComponentPool() {
			return new MockComponentPool() {
				@Override
				public void configure(Object component) {
				}
				@Override
				public void save(Object component)
						throws ComponentPersistException {
				}
			};
		}
		
		@Override
		public ArooaTools getTools() {
			return new StandardTools();
		}
	}
	
	/**
	 * Test using the set value method.
	 *
	 */
	public void testSetValue() {
		final SimpleBean obj = new SimpleBean();
		
		OurSession session = new OurSession();
		session.bean = obj;
		
		SetJob test = new SetJob();
		test.setArooaSession(session);
		
		ValueType value = new ValueType();
		value.setValue(new ArooaObject("Test"));
		
		test.setValues("obj.prop", value);
		
		test.run();
		
		assertEquals("Test", obj.prop);
	}
	
    public void testBasic() {
        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("Resource", 
        		getClass().getResourceAsStream("set-test.xml")));
        oj.run();
        
        CheckBasicSetters check = (CheckBasicSetters) new OddjobLookup(
        		oj).lookup("check");
        
        assertNotNull(check);
        assertEquals("Job state", JobState.COMPLETE, OddjobTestHelper.getJobState(check));
    }
    
    public static class MappedPropertyBean {
    	private Map<String, Object> map = 
    		new HashMap<String, Object>();
    	
    	public void setMapped(String name, Object value) {
    		map.put(name, value);
    	}
    	public Object getMapped(String name) {
    		return map.get(name);
    	}
    }

    public void testSetMapped() {
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <sequential>" +
    		"   <jobs>" +
    		"    <set>" +
    		"     <values>" +
    		"      <value key='test.mapped(akey)' value='test'/>" +
    		"     </values>" +
    		"    </set>" +
    		"    <bean class='" + MappedPropertyBean.class.getName() + "' id='test' />" +
    		"   </jobs>" + 
    		"  </sequential>" +
    		" </job>" +
    		"</oddjob>";

    	Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));

    	oddjob.run();

    	Object o = new OddjobLookup(oddjob).lookup("test");
    	MappedPropertyBean b = (MappedPropertyBean) o;

    	assertEquals(b.getMapped("akey"), "test");
    }
    
	static class MockDynaClass implements DynaClass {
		DynaProperty id = new DynaProperty("id", String.class);
		DynaProperty simple = new DynaProperty("simple", String.class);
		DynaProperty indexed = new DynaProperty("indexed", String[].class, String.class);
		DynaProperty mapped = new DynaProperty("mapped", Map.class, String.class);
		
		public DynaProperty[] getDynaProperties() {
			return new DynaProperty[] { id, simple, indexed, mapped };
		}
		public DynaProperty getDynaProperty(String name) {
			if (("id").equals(name)) {
				return id;
			}
			if (("simple").equals(name)) {
				return simple;
			}
			if (("indexed").equals(name)) {
				return indexed;
			}
			if (("mapped").equals(name)) {
				return mapped;
			}
			return null;
		}
		public String getName() {
			return toString();
		}
		public DynaBean newInstance() throws IllegalAccessException, InstantiationException {
			throw new RuntimeException("Unsupported");
		}
	}
    
    public static class MockDynaBean implements DynaBean {
    	String simple;
    	Map<String, Object> mapped = new HashMap<String, Object>();
    	String[] indexed = new String[2];
    	
    	DynaClass dynaClass = new MockDynaClass();
    	
    	public boolean contains(String name, String key) {
    		throw new RuntimeException("Unexpected.");
    	}
    	public Object get(String name) {
    		throw new RuntimeException("Unexpected.");
    	}
    	public Object get(String name, int index) {
    		throw new RuntimeException("Unexpected.");
    	}
    	public Object get(String name, String key) {
    		throw new RuntimeException("Unexpected.");
    	}
    	public DynaClass getDynaClass() {
    		return dynaClass;
    	}
    	public void remove(String name, String key) {
    		throw new RuntimeException("Unexpected.");
    	}
    	public void set(String name, int index, Object value) {
    		if (! "indexed".equals(name)) {
    			throw new RuntimeException("No index property");
    		}
    		indexed[index] = (String) value;
    	}
    	public void set(String name, Object value) {
    		if ("id".equals(name)) {
    			return;
    		}
    		if (! "simple".equals(name)) {
    			throw new RuntimeException("No simple property");
    		}
    		simple = (String) value;
    	}
    	public void set(String name, String key, Object value) {
    		if (! "mapped".equals(name)) {
    			throw new RuntimeException("No mapped property");
    		}
    		mapped.put(key,  value);
    	}
    }
    
    public static class MockDynaBeanArooa extends MockArooaBeanDescriptor {
    	
    	@Override
    	public ParsingInterceptor getParsingInterceptor() {
    		return null;
    	}
    	
    	@Override
    	public ConfiguredHow getConfiguredHow(String property) {
    		assertEquals("id", property);
    		return ConfiguredHow.ATTRIBUTE;
    	}
    	
    	@Override
    	public String getComponentProperty() {
    		return null;
    	}
    	
    	@Override
    	public ArooaAnnotations getAnnotations() {
    		return new NoAnnotations();
    	}
    }
    
    /**
     * Test the 3 set types on a DynaBean.
     *
     */
    public void testSetDynaBean() {
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <sequential>" +
    		"   <jobs>" +
    		"    <set>" +
    		"     <values>" +
    		"       <value key='test.simple' value='test'/>" +
    		"       <value key='test.indexed[0]' value='test'/>" +
    		"       <value key='test.mapped(akey)' value='test'/>" +
    		"     </values>" +
    		"    </set>" +
    		"    <bean class='" + MockDynaBean.class.getName() + "' id='test' />" +
    		"   </jobs>" + 
    		"  </sequential>" +
    		" </job>" +
    		"</oddjob>";

    	Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));

    	oddjob.run();

    	Object o = new OddjobLookup(oddjob).lookup("test");
    	
    	MockDynaBean b = (MockDynaBean) o;

    	assertEquals(b.simple, "test");
    	assertEquals(b.indexed[0], "test");
    	assertEquals(b.mapped.get("akey"), "test");
    }
    
}
