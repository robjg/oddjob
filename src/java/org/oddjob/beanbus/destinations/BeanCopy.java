package org.oddjob.beanbus.destinations;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.beanutils.MagicBeanClassCreator;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BusFilter;

/**
 * @oddjob.description Copy the properties of a bean to another bean.
 * 
 * @oddjob.example
 * 
 * Copy beans into bean properties given by the class.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanCopyJavaClass.xml}
 * 
 * @oddjob.example
 * 
 * Copy beans into a dynamically created bean.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanCopyMagicClass.xml}
 * 
 * @oddjob.example
 * 
 * Copy beans into a dynamically created bean the properties of which match
 * the source bean.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanCopyNoClass.xml}
 * 
 * @author rob
 *
 * @param <F> From
 * @param <T> To
 */
public class BeanCopy<F, T> extends AbstractDestination<F>
implements BusFilter<F, T>, ArooaSessionAware {

	private static AtomicInteger instance = new AtomicInteger();

	private String name;
	
	private ArooaClass arooaClass;
	
	private Collection<? super T> to;
	
	private PropertyAccessor accessor;
	
	private Map<String, String> mappings = 
			new LinkedHashMap<String, String>();
		
	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		ArooaTools tools = session.getTools();
		this.accessor = tools.getPropertyAccessor().accessorWithConversions(
				tools.getArooaConverter());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean add(F bean) {
		
		if (arooaClass == null) {
			arooaClass = createClassFromBean(bean);			
		}
		
		Object toBean = arooaClass.newInstance();
		
		
		for (Map.Entry<String, String> mapping : mappings.entrySet()) {
			
			String from = mapping.getKey();
			String to = mapping.getValue();
			
			accessor.setProperty(toBean, to, 
					accessor.getProperty(bean, from));
		}

		to.add((T) toBean);
		
		return true;
	}
	
	protected ArooaClass createClassFromBean(F bean) {
		
		MagicBeanClassCreator creator = new MagicBeanClassCreator(
				"BeanCopy-" + instance.incrementAndGet());
		
		ArooaClass sourceClass = accessor.getClassName(bean);
		BeanOverview overview = sourceClass.getBeanOverview(accessor);
		
		for (Map.Entry<String, String> mapping : mappings.entrySet()) {
			
			String from = mapping.getKey();
			String to = mapping.getValue();
			
			Class<?> propertyType = overview.getPropertyType(from);
			
			creator.addProperty(to, propertyType);
		}
		
		return creator.create();
	}
	
	public void setArooaClass(ArooaClass arooaClass) {
		this.arooaClass = arooaClass;
	}
	
	@Override
	public void setTo(Collection<? super T> to) {
		this.to = to;
	}
	
	public Collection<? super T> getTo() {
		return to;
	}
	
	public void setMappings(String from, String to) {
		mappings.put(from, to);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}
}
