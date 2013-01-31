package org.oddjob.beanbus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.beanutils.MagicBeanClassCreator;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;

public class BeanCopy<F, T> 
implements Section<F, T>, ArooaSessionAware {

	private static AtomicInteger instance = new AtomicInteger();
	
	private ArooaClass arooaClass;
	
	private Destination<? super T> to;
	
	private PropertyAccessor accessor;
	
	private Map<String, String> mappings = 
			new LinkedHashMap<String, String>();
		
	@Override
	public void setArooaSession(ArooaSession session) {
		ArooaTools tools = session.getTools();
		this.accessor = tools.getPropertyAccessor().accessorWithConversions(
				tools.getArooaConverter());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void accept(F bean) throws BadBeanException, BusCrashException {
		
		if (arooaClass == null) {
			arooaClass = createClassFromBean(bean);			
		}
		
		Object toBean = arooaClass.newInstance();
		
		
		for (Map.Entry<String, String> mapping : mappings.entrySet()) {
			
			String from = mapping.getKey();
			String to = mapping.getValue();
			
			accessor.setSimpleProperty(toBean, to, 
					accessor.getProperty(bean, from));
		}

		to.accept((T) toBean);
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
	public void setTo(Destination<? super T> to) {
		this.to = to;
	}
	
	public Destination<? super T> getTo() {
		return to;
	}
	
	public void setMappings(String from, String to) {
		mappings.put(from, to);
	}
}
