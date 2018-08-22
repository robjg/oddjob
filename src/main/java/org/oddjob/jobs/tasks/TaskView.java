package org.oddjob.jobs.tasks;

import org.oddjob.Stateful;

/**
 * Provide a view on the progress of a Task.
 * 
 * @author rob
 *
 */
public interface TaskView extends Stateful {

	Object getTaskResponse();
}
