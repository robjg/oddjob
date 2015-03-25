package org.oddjob.scheduling.state;

import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.State;

/**
 * Encapsulate the allowed states for Timers.
 * 
 * @author Rob Gordon
 */
public enum TimerState implements State {
	
	/**
	 * The timer can be started. A timer can not be started unless it
	 * is in this state.
	 */	
	STARTABLE() {
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
	 * The timer is starting.
	 */	
	STARTING() {
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
	 * Indicates that child jobs are active. A Timer will only show this
	 * state when it has completed but a child job is being re-run.
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
	 * The timer has started.
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
	 * The timer is complete but is reflecting that the child job is 
	 * incomplete.
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
	 * The timer is complete and the child job is complete. 
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
	 * The timer failed to Start or the timer is complete but is reflecting
	 * the child's exception state.
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
	 * The timer has been destroyed. It can no longer be used.
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
	 * Register Icons for these states.
	 */
	static {
		
		StateIcons.register(TimerState.STARTABLE, IconHelper.STARTABLE);
		StateIcons.register(TimerState.STARTING, IconHelper.EXECUTING);
		StateIcons.register(TimerState.ACTIVE, IconHelper.ACTIVE);
		StateIcons.register(TimerState.STARTED, IconHelper.STARTED);
		StateIcons.register(TimerState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		StateIcons.register(TimerState.COMPLETE, IconHelper.COMPLETE);
		StateIcons.register(TimerState.EXCEPTION, IconHelper.EXCEPTION);
		StateIcons.register(TimerState.DESTROYED, IconHelper.INVALID);
		
	}
}

