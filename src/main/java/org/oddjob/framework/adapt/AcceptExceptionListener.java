package org.oddjob.framework.adapt;

import org.oddjob.framework.adapt.service.ServiceStrategies;

import java.beans.ExceptionListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate that a method can accept an {@link ExceptionListener}.
 * 
 * @see org.oddjob.framework.FallibleComponent
 * @see ServiceStrategies
 * 
 * @author rob
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AcceptExceptionListener {

}
