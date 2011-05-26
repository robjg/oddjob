/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.state;

import org.oddjob.Stateful;

/**
 * A wrapper for a job that holds the state of the job
 * after it's been executed.
 * 
 * @author Rob Gordon.
 */
public class StateMemory implements JobStateListener {
    private volatile JobState jobState;
    private volatile Throwable t;

    public void jobStateChange(JobStateEvent event) {
    	// only save the first state change after the
    	// job finishes executing - stops the unlikely
    	// event that the job is reset before our listener
    	// is removed.
    	if (jobState == null || jobState == JobState.READY
    			|| jobState == JobState.EXECUTING) {
    		jobState = event.getJobState();
    		t = event.getException();
    	}
    }
		    
    public JobState getJobState() {
        return jobState;
    }
		    
    public Throwable getThrowable() {
    	return t;
    }
    
    public void run(Runnable job) {
        if (job instanceof Stateful) {
            ((Stateful)job).addJobStateListener(this);
        } else {
            jobState = JobState.COMPLETE;
        }
        try {
            job.run();
        }
        finally {
            if (job instanceof Stateful) {
                ((Stateful)job).removeJobStateListener(this);
            }	            
        }
    }
}

