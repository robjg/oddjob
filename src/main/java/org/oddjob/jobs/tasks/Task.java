package org.oddjob.jobs.tasks;

import java.io.Serializable;
import java.util.Properties;

/**
 * Define a Task.
 * <p>
 * TODO: Add identification information.
 * 
 * @author rob
 *
 */
public interface Task extends Serializable {

	Properties getProperties();
}
