package org.oddjob.framework.adapt.job;

import org.oddjob.framework.adapt.AdaptorFactory;

/**
 * Something that can attempt to adapt a component to be a job.
 * 
 * @author rob
 *
 */
public interface JobStrategy extends AdaptorFactory<JobAdaptor> {

}