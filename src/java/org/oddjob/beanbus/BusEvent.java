package org.oddjob.beanbus;

import java.util.EventObject;

/**
 * An event on the bus.
 * 
 * @author rob
 *
 */
public class BusEvent extends EventObject {
	private static final long serialVersionUID = 2010021800L;
	
	/**
	 * Constructor.
	 *  
	 * @param source The source of the event.
	 */
	public BusEvent(BusConductor source) {
		super(source);
	}
	
	@Override
	public BusConductor getSource() {
		return (BusConductor) super.getSource();
	}
}
