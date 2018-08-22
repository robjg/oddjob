package org.oddjob.events.state;

import org.oddjob.events.EventSource;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.State;

/**
 * Encapsulate the allowed states for a {@link EventSource}.
 * 
 * @author Rob Gordon
 */
public enum EventState implements State {
	
	/**
	 * The Node is ready to be started. 
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
	 * The node is connecting to it's event source.
	 */	
	CONNECTING() {
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
	 * A node is waiting for its first event.
	 */	
	WAITING() {
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
	 * A node is receiving an event.
	 */	
	FIRING() {
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
	 * An event has arrived but we are still waiting for more.
	 */	
	TRIGGERED() {
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
	 * The node has stopped subscribing but didn't receive an event.
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
	 * The node has stopped subscribing but didn receive an event.
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
	 * Indicates an exception has occurred. 
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
	 * Register Icons for these states.
	 */
	static {
		
		StateIcons.register(EventState.READY, IconHelper.STARTABLE);
		StateIcons.register(EventState.CONNECTING, IconHelper.EXECUTING);
		StateIcons.register(EventState.WAITING, IconHelper.WAITING);
		StateIcons.register(EventState.FIRING, IconHelper.FIRING);
		StateIcons.register(EventState.TRIGGERED, IconHelper.TRIGGERED);
		StateIcons.register(EventState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		StateIcons.register(EventState.COMPLETE, IconHelper.COMPLETE);
		StateIcons.register(EventState.EXCEPTION, IconHelper.EXCEPTION);
		StateIcons.register(EventState.DESTROYED, IconHelper.INVALID);
		
	}
	
}

