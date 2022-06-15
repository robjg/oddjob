package org.oddjob.framework.adapt.job;

import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.framework.adapt.ComponentAdapter;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Something that adapts a component to be a {@link Runnable} job.
 *  
 * @author rob
 *
 */
public interface JobAdaptor
extends Callable<Integer>, ComponentAdapter, ArooaSessionAware, Serializable {
}
