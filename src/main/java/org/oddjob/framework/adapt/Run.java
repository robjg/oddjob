package org.oddjob.framework.adapt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate that a method is the run method of a job.
 * 
 * @see org.oddjob.framework.adapt.job.JobAdaptor
 * @see org.oddjob.framework.adapt.job.JobStrategies
 * 
 * @author rob
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Run {

}
