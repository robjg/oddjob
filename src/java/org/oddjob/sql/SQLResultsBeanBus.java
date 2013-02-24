package org.oddjob.sql;

import java.util.Collection;

import org.oddjob.beanbus.BeanBus;

/**
 * @oddjob.description A {@link SQLResultHandler} that attaches to 
 * {@link BeanBus} components.
 * 
 * @author rob
 */
public class SQLResultsBeanBus extends BeanFactoryResultHandler {
	
	private volatile Collection<? super Object> to;
	
	@Override
	protected void accept(Object bean) {
		
		to.add(bean);
	}
	
	public void setTo(Collection<? super Object> to) {
		this.to = to;
	}
	
	public Collection<? super Object> getTo() {
		return to;
	}
}
