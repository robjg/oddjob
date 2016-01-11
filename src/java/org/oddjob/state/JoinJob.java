package org.oddjob.state;

import java.util.concurrent.atomic.AtomicReference;

import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description
 * 
 * Waits for a COMPLETE state from it's child job before allowing
 * the thread of execution to continue.
 * <p>
 * 
 * @oddjob.example
 * 
 * An join that waits for two triggers. In this example another trigger
 * to run the last job might be a better solution because it wouldn't hog
 * a thread - but there are situations when join is just simpler.
 * 
 * {@oddjob.xml.resource org/oddjob/state/JoinExample.xml}
 * 
 * @author Rob Gordon
 */
public class JoinJob extends StructuralJob<Runnable> {
	
	private static final long serialVersionUID = 2010081600L;
	
	
	private volatile long timeout;
	
	/**
	 * Set the child job.
	 * 
	 * @oddjob.property job
	 * @oddjob.description The child job.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param child A child
	 */
	@ArooaComponent
	public void setJob(Runnable child) {
	    
		if (child == null) {
		    logger().debug("Removing child.");
			childHelper.removeAllChildren();
		}
		else {
		    logger().debug("Adding child [" + child + "]");
		    if (childHelper.getChild() != null) {
		        throw new OddjobConfigException(
		                "Join can't have more than one child component.");
		    }
			childHelper.insertChild(0, child);
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected void execute() throws InterruptedException {
		
		Runnable child = childHelper.getChild();
		
		if (child == null) {
			return;
		}
		
		child.run();
		
		final AtomicReference<State> state = new AtomicReference<State>();
		StateListener listener = new StateListener() {
			@Override
			public void jobStateChange(StateEvent event) {
				state.set(event.getState());
				if (!event.getState().isStoppable()) {
					synchronized (JoinJob.this) {
						JoinJob.this.notifyAll();
					}
				}
			}
		};
		((Stateful) child).addStateListener(listener);
		
		try {
			if (!stop && state.get().isStoppable()) {			
				synchronized(this) {
					wait(timeout);
				}
			}
			if (!stop && state.get().isStoppable()) {			
				throw new IllegalStateException("Join failed within timeout of " + timeout);
			}
		} 
		finally {
			removeStateListener(listener);
		}
		
		stop = false;
	}		
	
	@Override
	protected StateOperator getInitialStateOp() {
		return new AnyActiveStateOp();
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
