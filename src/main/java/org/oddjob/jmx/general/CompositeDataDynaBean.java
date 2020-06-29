package org.oddjob.jmx.general;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for {@link CompositeData} so it can be accessed with
 * BeanUtils simple property expressions.
 * 
 * @author rob
 *
 */
public class CompositeDataDynaBean implements DynaBean {

	private final CompositeData data;
	
	private final ThisDynaClass dynaClass;
	
	public CompositeDataDynaBean(CompositeData data) {
		this.data = data;
		this.dynaClass = new ThisDynaClass(data.getCompositeType());
	}
	
	
	@Override
	public boolean contains(String arg0, String arg1) {
		return false;
	}

	@Override
	public Object get(String name) {
		Object result = data.get(name);
		if (result instanceof CompositeData) {
			return new CompositeDataDynaBean(
					(CompositeData) result);
		}
		else {
			return result;
		}
	}

	@Override
	public Object get(String arg0, int arg1) {
		throw new RuntimeException("No indexed properties.");
	}

	@Override
	public Object get(String arg0, String arg1) {
		throw new RuntimeException("No mapped properties.");
	}

	@Override
	public DynaClass getDynaClass() {
		return dynaClass;
	}

	@Override
	public void remove(String arg0, String arg1) {
		throw new RuntimeException("No mapped properties.");
	}

	@Override
	public void set(String name, Object value) {
		throw new RuntimeException("Property " + name + " isn't writable.");
	}

	@Override
	public void set(String arg0, int arg1, Object arg2) {
		throw new RuntimeException("No indexed properties.");
	}

	@Override
	public void set(String arg0, String arg1, Object arg2) {
		throw new RuntimeException("No mapped properties.");
	}
	
	
	@Override
	public String toString() {
		return "CompositeData: " + Arrays.toString(dynaClass.propertyNames);
	}
	
	/**
	 * The {@link DynaClass} implementation.
	 */
	private class ThisDynaClass implements Serializable, DynaClass {
		private static final long serialVersionUID = 2012087200L;
		
		private final String[] propertyNames;
		
		private final DynaProperty[] properties;
		
		private final Map<String, DynaProperty> map =
				new HashMap<>();
			
		public ThisDynaClass(CompositeType type) {

			Set<String> keySet = type.keySet();
			this.propertyNames = new String[keySet.size()];
			this.properties = new DynaProperty[keySet.size()];
			
			int i = 0;
			for (String key: keySet) {

				propertyNames[i] = key;
						
				DynaProperty property = 
						new DynaProperty(key, Object.class); 
				
				properties[i] = property;
				map.put(property.getName(), property);
				
				++i;
			}
		}
		
		@Override
		public DynaProperty[] getDynaProperties() {
			return properties;
		}
		
		@Override
		public DynaProperty getDynaProperty(String name) {
			return map.get(name);
		}
		
		@Override
		public String getName() {
			return CompositeDataDynaBean.this.toString() ;
		}
		
		@Override
		public DynaBean newInstance() throws
				InstantiationException {
			throw new InstantiationException(
					"Can't create new " + getName());
		}
	}
	
}
