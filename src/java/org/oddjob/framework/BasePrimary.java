package org.oddjob.framework;


import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.logging.LogEnabled;

/**
 * An abstract implementation of a component which provides common functionality to
 * concrete sub classes.
 * 
 * @author Rob Gordon
 */

public abstract class BasePrimary extends BaseComponent
implements LogEnabled {
	
    /** provides a unique logger per component. */
    private static int instanceCount;

    /** The logger. */
    private Logger theLogger;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;
	
	/**
	 * Allow sub classes access to the logger.
	 * 
	 * @return The logger.
	 */
	protected Logger logger() {
	    if (theLogger == null) {
	    	int count = 0;
	    	synchronized (BaseComponent.class) {
	    		count = instanceCount++;
	    	}
	        theLogger = Logger.getLogger(this.getClass().getName() 
	                + "." + count);
	    }
	    return theLogger;
	}
		
		
	protected void configure() 
	throws ArooaConfigurationException {
		configure(this);
	}
	
	protected void save() throws ComponentPersistException {
		save(this);
	}
	
	/**
	 * Set the job name. Used by subclasses to set the job name.
	 * 
	 * @param name The name of the job.
	 */
	synchronized public void setName(String name) {
		stateHandler().assertAlive();
		
		String old = this.name;
		this.name = name;
		firePropertyChange("name", old, name);
	}

	/**
	 * Get the job name.
	 * 
	 * @return The job name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return Returns the logger.
	 */
	public String loggerName() {
		return logger().getName();
	}
	
	/**
	 * Rename the logger. Used after de-serialisation.
	 *  
	 * @param logger The logger to set.
	 */
	protected void logger(String logger) {
		if (logger == null) {
			return;
		}
		if (theLogger != null) {
			theLogger.debug("Logger being replaced by [" + logger + "]");
		}
		theLogger = Logger.getLogger(logger); 
	}
	
	/**
	 * Override toString.
	 */	
	public String toString() {
	    if (getName() == null) {
	        return getClass().getSimpleName();
	    }
	    else {
	        return getName();
	    }
	}
	
}
