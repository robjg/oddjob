package org.oddjob.state;

import org.oddjob.Structural;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;

/**
 * Encapsulate the allowed states for a {@link Structural}.
 * 
 * @author Rob Gordon
 */
public enum ParentState implements State {
	
	/**
	 * The job is ready to be executing. The Oddjob Framework
	 * will only execute a job which is in this state.
	 */	
	READY() {
		@Override
		public boolean isReady() {
			return true;
		}
		@Override
		public boolean isExecuting() {
			return false;
		}
		@Override
		public boolean isStoppable() {
			return false;
		}
		@Override
		public boolean isComplete() {
			return false;
		}
		@Override
		public boolean isIncomplete() {
			return false;
		}
		@Override
		public boolean isException() {
			return false;
		}
		@Override
		public boolean isDestroyed() {
			return false;
		}
	},

	/**
	 * Indicates the job is executing, which normally means it's 
	 * in the process of executing it's children.
	 */	
	EXECUTING() {
		@Override
		public boolean isReady() {
			return false;
		}
		@Override
		public boolean isExecuting() {
			return true;
		}
		@Override
		public boolean isStoppable() {
			return true;
		}
		@Override
		public boolean isComplete() {
			return false;
		}
		@Override
		public boolean isIncomplete() {
			return false;
		}
		@Override
		public boolean isException() {
			return false;
		}
		@Override
		public boolean isDestroyed() {
			return false;
		}
	},

	/**
	 * Indicates that child jobs are active. This will be the case when
	 * the execution thread has passed on but a child job is 
	 * still active or executing.
	 */	
	ACTIVE() {
		@Override
		public boolean isReady() {
			return false;
		}
		@Override
		public boolean isExecuting() {
			return false;
		}
		@Override
		public boolean isStoppable() {
			return true;
		}
		@Override
		public boolean isComplete() {
			return false;
		}
		@Override
		public boolean isIncomplete() {
			return false;
		}
		@Override
		public boolean isException() {
			return false;
		}
		@Override
		public boolean isDestroyed() {
			return false;
		}
	},
	
	/**
	 * Indicates that child jobs are stoppable but done. This will be the 
	 * case for service for {@link ServiceState}s. This state differs from
	 * {@link #ACTIVE} in that it will cause the next sibling to execute
	 * in a {@link CascadeJob}.
	 */	
	STARTED() {
		@Override
		public boolean isReady() {
			return false;
		}
		@Override
		public boolean isExecuting() {
			return false;
		}
		@Override
		public boolean isStoppable() {
			return true;
		}
		@Override
		public boolean isComplete() {
			return true;
		}
		@Override
		public boolean isIncomplete() {
			return false;
		}
		@Override
		public boolean isException() {
			return false;
		}
		@Override
		public boolean isDestroyed() {
			return false;
		}
	},
	
	/**
	 * Indicates the job is not complete. This will typically be the case 
	 * when one or more child jobs are INCOMPLETE, but this depends on 
	 * the nature of the Structural job and the {@link StateOperator} it
	 * uses.
	 */
	INCOMPLETE() {
		@Override
		public boolean isReady() {
			return false;
		}
		@Override
		public boolean isExecuting() {
			return false;
		}
		@Override
		public boolean isStoppable() {
			return false;
		}
		@Override
		public boolean isComplete() {
			return false;
		}
		@Override
		public boolean isIncomplete() {
			return true;
		}
		@Override
		public boolean isException() {
			return false;
		}
		@Override
		public boolean isDestroyed() {
			return false;
		}
	},
	
	/**
	 * Indicates job has completed. The will typically be the case when
	 * all child jobs have completed, but this depends on 
	 * the nature of the Structural job and the {@link StateOperator} it
	 * uses. 
	 */	
	COMPLETE() {
		@Override
		public boolean isReady() {
			return false;
		}
		@Override
		public boolean isExecuting() {
			return false;
		}
		@Override
		public boolean isStoppable() {
			return false;
		}
		@Override
		public boolean isComplete() {
			return true;
		}
		@Override
		public boolean isIncomplete() {
			return false;
		}
		@Override
		public boolean isException() {
			return false;
		}
		@Override
		public boolean isDestroyed() {
			return false;
		}
	},
	
	/**
	 * Indicates an exception has occurred. This will typically be the case
	 * when one of a jobs child jobs is in an EXCEPTION state, but 
	 * this depends on 
	 * the nature of the Structural job and the {@link StateOperator} it
	 * uses.
	 */	
	EXCEPTION() {
		@Override
		public boolean isReady() {
			return false;
		}
		@Override
		public boolean isExecuting() {
			return false;
		}
		@Override
		public boolean isStoppable() {
			return false;
		}
		@Override
		public boolean isComplete() {
			return false;
		}
		@Override
		public boolean isIncomplete() {
			return false;
		}
		@Override
		public boolean isException() {
			return true;
		}
		@Override
		public boolean isDestroyed() {
			return false;
		}
	},
	
	/**
	 * The job has been destroyed. It can no longer be used.
	 */	
	DESTROYED() {
		@Override
		public boolean isReady() {
			return false;
		}
		@Override
		public boolean isExecuting() {
			return false;
		}
		@Override
		public boolean isStoppable() {
			return false;
		}
		@Override
		public boolean isComplete() {
			return false;
		}
		@Override
		public boolean isIncomplete() {
			return false;
		}
		@Override
		public boolean isException() {
			return false;
		}
		@Override
		public boolean isDestroyed() {
			return true;
		}
	},
	;

	static {

		StateIcons.register(ParentState.READY, IconHelper.READY);
		StateIcons.register(ParentState.EXECUTING, IconHelper.EXECUTING);
		StateIcons.register(ParentState.ACTIVE, IconHelper.ACTIVE);
		StateIcons.register(ParentState.STARTED, IconHelper.STARTED);
		StateIcons.register(ParentState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		StateIcons.register(ParentState.COMPLETE, IconHelper.COMPLETE);
		StateIcons.register(ParentState.EXCEPTION, IconHelper.EXCEPTION);
		StateIcons.register(ParentState.DESTROYED, IconHelper.INVALID);

	}

}

