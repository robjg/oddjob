package org.oddjob.jobs.tasks;

import org.oddjob.state.State;

/**
 * Encapsulate the allowed states for a {@link Task}.
 * 
 * @author Rob Gordon
 */
public enum TaskState implements State {

	/**
	 * The task hasn't started.
	 */
	PENDING() {
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
	 * In progress
	 */
	INPROGRESS() {
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
	 * Indicates the task failed to complete.
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
	 * Indicates task has completed successfully.
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
}
