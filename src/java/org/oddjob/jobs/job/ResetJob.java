package org.oddjob.jobs.job;

import org.oddjob.Resetable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SimpleJob;
import org.oddjob.persist.FilePersister;

/**
 * @oddjob.description A job which resets another job. This job is 
 * useful to reset jobs or services that have been persisted, and
 * loaded back in their previous COMPLETE states. The reset 
 * can be used to set them back to READY.
 * <p>
 * A reset might also be needed before running a job elsewhere
 * such as on a remote server.
 * <p>
 * This job is not Serializable and so won't be persisted
 * itself.
 * <p>
 * See also the {@link org.oddjob.state.Resets} job.
 *
 *
 * @oddjob.example 
 * 
 * Using reset in explorer.xml. 
 * <p>
 * Look at the explorer.xml file in Oddjob's home directory. This file is 
 * loaded by the default oddjob.xml file when Oddjob first runs.
 * The explorer.xml configuration is run with a {@link FilePersister} 
 * persister that persists the Explorers state when it 
 * completes. When Oddjob is run again the Explorer will be
 * loaded with it's previous COMPLETE state and so won't run. The reset
 * is necessary to set it back to READY. 
 *
 * @author Rob Gordon
 */
public class ResetJob extends SimpleJob {

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
		if (job == null) {
			throw new NullPointerException("No Job");
		}
		
    	String level = this.level.toLowerCase();
        if (level == null) {
            level = "soft";
        }
        if (!"hard".equals(level) && !"soft".equals(level)) {
            throw new IllegalArgumentException("Level must be hard or soft.");
        }
        
		logger().info("Sending " + level + " reset to [" + job + "]");
		
		if (level.equals("soft")) {
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
        return level;
    }
    
    /**
     * @param level The level to set.
     */
    public void setLevel(String level) {
        this.level = level;
    }
}
