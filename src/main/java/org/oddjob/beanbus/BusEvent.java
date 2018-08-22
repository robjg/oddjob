package org.oddjob.beanbus;

import java.util.EventObject;

/**
 * An event on the bus.
 * 
 * @See BusListener
 * 
 * @author rob
 *
 */
public class BusEvent extends EventObject {
	private static final long serialVersionUID = 2010021800L;
	
	private final BusPhase phase;
	
	private final Exception busCrashException;
	
	/**
	 * Constructor.
	 *  
	 * @param source The source of the event.
	 * @param phase The phase that caused the event.
	 */
	public BusEvent(BusConductor source, BusPhase phase) {
		this(source, phase, null);
	}
	
	/**
	 * Constructor for a bus crash event.
	 * 
	 * @param source The source of the event.
	 * @param phase The phase that caused the event.
	 * @param busCrashException The exception that cause the crash.
	 */
	public BusEvent(BusConductor source, BusPhase phase, 
			Exception busCrashException) {
		super(source);
		this.phase = phase;
		this.busCrashException = busCrashException;
	}
	
	@Override
	public BusConductor getSource() {
		return (BusConductor) super.getSource();
	}
	
	public BusPhase getPhase() {
		return phase;
	}
	
	public Exception getBusCrashException() {
		return busCrashException;
	}
}
