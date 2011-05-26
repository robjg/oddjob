package org.oddjob.beanbus;

import java.util.EventObject;

public class BusEvent extends EventObject {
	private static final long serialVersionUID = 2010021800L;
	
	public BusEvent(BeanBus source) {
		super(source);
	}
	
	@Override
	public BeanBus getSource() {
		return (BeanBus) super.getSource();
	}
}
