package org.oddjob.state;

/**
 * Encapsulate the allowed states for a service.
 * 
 * @author Rob Gordon
 */
public enum ServiceState implements State {
	
	/**
	 * The service can be started. 
	 * <p>
	 * Note that this state is needed in addition
	 * to {@link #STOPPED} to tie in with state transitions of job states.
	 * Without this state, an {@link #EXCEPTION} state would reset to 
	 * {@code STOPPED} which could cause a parent sequence to complete
	 * which would be counter intuitive compared to the behaviour of
	 * job states. 
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
	 * Indicates the service is starting. The execution thread is
	 * still in the start method.
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
	 * Indicates the service has started and is now available to service
	 * requests.
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
	 * The service is stopped. 
	 */	
	STOPPED() {
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
	 * Indicates an exception has occurred. This is generally when
	 * trying to start the service. But may also happen at other times.
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
	 * The service has been destroyed. It can no longer be used.
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
	public static ServiceState stateFor(String state) {
		state = state.toUpperCase();
		return valueOf(state);
	}
}

