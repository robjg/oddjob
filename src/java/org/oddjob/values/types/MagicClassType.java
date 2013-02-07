package org.oddjob.values.types;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.oddjob.arooa.beanutils.MagicBeanClassCreator;
import org.oddjob.arooa.beanutils.MagicBeanDescriptorFactory;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.beanbus.BeanCopy;

/**
 * @oddjob.description Definition for a Magic Bean, which is a bean that 
 * can be defined dynamically. 
 * <p>
 * See also {@link MagicBeanDescriptorFactory}.
 * 
 * @oddjob.example Using a magic-class to create a magic bean.
 * 
 * {@oddjob.xml.resource org/oddjob/values/types/MagicBeanTypeExample.xml}
 * 
 * @oddjob.exemple See also {@link BeanCopy}
 * 
 * @author rob
 *
 */
public class MagicClassType implements ValueFactory<ArooaClass>{
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The name of the class. 
	 * @oddjob.required Yes.
	 */
	private String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The bean properties. A mapping of name to 
	 * class name.
	 * @oddjob.required No.
	 */
	private final Map<String, String> properties = 
		new LinkedHashMap<String, String>();
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The class loader. 
	 * @oddjob.required No. Set automatically by Oddjob.
	 */
	private ClassLoader classLoader;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProperties(String key, String className) {
		
		if (key == null) {
			properties.remove(key);
		}
		else {
			properties.put(key, className);
		}
	}
	
	public String getProperties(String key) {
		return properties.get(key);
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Inject
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public ArooaClass toValue() throws ArooaConversionException {
		
		MagicBeanClassCreator classCreator = new MagicBeanClassCreator(name);
		
		for (Map.Entry<String, String> prop : properties.entrySet()) {
						
			String className = prop.getValue();
			String propertyName = prop.getKey();
			Class<?> cl;
			if (className == null) {
				cl = Object.class;
			}
			else {
				try {
					cl = Class.forName(className, true, classLoader);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("For MagicBean class " + 
							name + ", property " + propertyName, e);
				}
			}
			
			classCreator.addProperty(propertyName, cl);
		}
				
		return classCreator.create();
	}	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + name;
	}
}
