package org.oddjob.framework.adapt;

import java.beans.ExceptionListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.oddjob.framework.AsynchronousJob;
import org.oddjob.framework.adapt.service.ServiceStrategies;

/**
 * Annotate that a method can accept an {@link ExceptionListener}.
 * 
 * @see AsynchronousJob
 * @see ServiceStrategies
 * 
 * @author rob
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AcceptExceptionListener {

}
