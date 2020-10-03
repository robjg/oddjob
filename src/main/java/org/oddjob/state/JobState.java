package org.oddjob.state;

import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;

/**
 * Encapsulate the allowed states for a job.
 * 
 * @author Rob Gordon
 */
public enum JobState implements State {
	
	/**
	 * Indicates the job is ready to be executing. The Oddjob Framework
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
	 * Indicates the job is executing.
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
	 * An Asynchronous Job is waiting to complete.
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
	 * Indicates the job is not complete. Typically this is not unexpected,
	 * for instance a job which looks for a file, and a
	 * parent job will re-execute the job again at a later date. 
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
	 * Indicates job has completed. 
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
	 * Indicates an exception has occurred. This is generally 
	 * recoverable. Such as database failure, disk full etc.
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
	
	/**
	 * Utility function to convert a state text to to the JobState.
	 * 
	 * @param state Case insensitive text.
	 * @return The corresponding jobState or null if it's invalid.
	 */
	public static JobState stateFor(String state) {
		state = state.toUpperCase();
		return valueOf(state);
	}

	static {

		StateIcons.register(JobState.READY, IconHelper.READY);
		StateIcons.register(JobState.EXECUTING, IconHelper.EXECUTING);
		StateIcons.register(JobState.ACTIVE, IconHelper.ACTIVE);
		StateIcons.register(JobState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		StateIcons.register(JobState.COMPLETE, IconHelper.COMPLETE);
		StateIcons.register(JobState.EXCEPTION, IconHelper.EXCEPTION);
		StateIcons.register(JobState.DESTROYED, IconHelper.INVALID);
	}
}

