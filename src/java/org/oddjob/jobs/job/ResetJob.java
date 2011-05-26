package org.oddjob.jobs.job;

import org.oddjob.Resetable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SimpleJob;

/**
 * @oddjob.description A job which resets another job.
 *
 * @author Rob Gordon
 */

public class ResetJob extends SimpleJob {
    private static final long serialVersionUID = 20050806;

	/** 
	 * @oddjob.property 
	 * @oddjob.description Job to reset.
	 * @oddjob.required Yes.
	 */
	private transient Resetable job;

	/** 
	 * @oddjob.property
	 * @oddjob.description The reset level, hard or soft 
	 * @oddjob.required No, defaults to soft.
	 */
	private transient String level;
	
	/**
	 * Set the stop node directly.
	 * 
	 * @param node The node to stop.
	 */
	@ArooaAttribute
	synchronized public void setJob(Resetable node) {
		if (node == null ) {
			throw new NullPointerException("Job to Reset must not be null.");
		}
		this.job = node;
	}

	/**
	 * Get the node to stop.
	 * 
	 * @return The node.
	 */
	synchronized public Resetable getJob() {
		return this.job;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		if (getLevel().equals("soft")) {
		    ((Resetable)job).softReset();
		}
		else {
		    ((Resetable)job).hardReset();
		}
		return 0;	
	}
	
    /**
     * @return Returns the level.
     */
    public String getLevel() {
        if (level == null) {
            level = "soft";
        }
        return level;
    }
    
    /**
     * @param level The level to set.
     */
    public void setLevel(String level) {
    	level = level.toLowerCase();
        if (!"hard".equals(level) && !"soft".equals(level)) {
            throw new IllegalArgumentException("Level must be hard or soft.");
        }
        this.level = level;
    }
}
