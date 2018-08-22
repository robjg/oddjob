package org.oddjob.values.types;

import java.util.LinkedHashMap;
import java.util.Map;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.beanutils.MagicBeanClassCreator;
import org.oddjob.arooa.beanutils.MagicBeanDescriptorFactory;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.types.ValueFactory;

/**
 * Create an on the fly bean. Useful for testing. See also 
 * {@link MagicClassType} and {@link MagicBeanDescriptorFactory}.
 * 
 * @author rob
 *
 */
public class MagicBeanType 
implements ValueFactory<Object>, ArooaSessionAware {

	private ArooaClass magicClass;
	
	private Map<String, ArooaValue> properties = 
		new LinkedHashMap<String, ArooaValue>();
	
	private ArooaSession session;
	
	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public Object toValue() throws ArooaConversionException {
		
		ArooaClass magicClass = this.magicClass;
		if (magicClass == null) {
			
			MagicBeanClassCreator creator = new MagicBeanClassCreator(
					"MagicBeanType");
			
			for (String property : properties.keySet()) {
				creator.addProperty(property, Object.class);
			}
			
			magicClass = creator.create();
		}
		
		Object bean = magicClass.newInstance();
		
		PropertyAccessor accessor = session.getTools().getPropertyAccessor(
				).accessorWithConversions(session.getTools(
						).getArooaConverter());
		
		for (Map.Entry<String, ArooaValue> property : properties.entrySet()) {
			
			ArooaValue value = property.getValue();
			
			accessor.setProperty(bean, property.getKey(), value);
		}
		
		return bean;
	}

	public ArooaClass getMagicClass() {
		return magicClass;
	}

	public void setMagicClass(ArooaClass magicClass) {
		this.magicClass = magicClass;
	}
	
	public void setProperties(String name, ArooaValue value) {
		if (value == null) {
			properties.remove(name);
		}
		else {
			properties.put(name, value);
		}
	}
	
	public ArooaValue getProperties(String name) {
		return properties.get(name);
	}
	
}
