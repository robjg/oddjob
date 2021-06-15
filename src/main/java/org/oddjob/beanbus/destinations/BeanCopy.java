package org.oddjob.beanbus.destinations;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.beanutils.MagicBeanClassCreator;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.beanbus.BusFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
public class BeanCopy<F, T>
implements Consumer<F>, BusFilter<F, T>, ArooaSessionAware {
	private static final Logger logger = LoggerFactory.getLogger(BeanCopy.class);

	private static final AtomicInteger instance = new AtomicInteger();

	private String name;
	
	private ArooaClass arooaClass;
	
	private Consumer<? super T> to;
	
	private PropertyAccessor accessor;
	
	private final Map<String, String> mappings =
			new LinkedHashMap<>();
		
	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		ArooaTools tools = session.getTools();
		this.accessor = tools.getPropertyAccessor().accessorWithConversions(
				tools.getArooaConverter());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void accept(F bean) {
		
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

		to.accept((T) toBean);
	}
	
	protected ArooaClass createClassFromBean(F bean) {
		
		String magicClassName = "BeanCopy-" + instance.incrementAndGet();
		
		logger.debug("Creating Magic Bean Class [" + magicClassName + "]");
		
		MagicBeanClassCreator creator = new MagicBeanClassCreator(
				magicClassName);
		
		ArooaClass sourceClass = accessor.getClassName(bean);
		BeanOverview overview = sourceClass.getBeanOverview(accessor);
		
		for (Map.Entry<String, String> mapping : mappings.entrySet()) {
			
			String from = mapping.getKey();
			String to = mapping.getValue();
			
			Class<?> propertyType = overview.getPropertyType(from);
			
			logger.debug("Adding property to copy [" + to + 
					"] of type [" + propertyType + "]");
			
			creator.addProperty(to, propertyType);
		}
		
		return creator.create();
	}
	
	public void setArooaClass(ArooaClass arooaClass) {
		this.arooaClass = arooaClass;
	}
	
	@Override
	public void setTo(Consumer<? super T> to) {
		this.to = to;
	}
	
	public Consumer<? super T> getTo() {
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
