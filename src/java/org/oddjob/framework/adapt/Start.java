package org.oddjob.framework.adapt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.oddjob.framework.Service;
import org.oddjob.framework.adapt.service.ServiceStrategies;

/**
 * Annotate that a method is the start method of a service.
 * 
 * @see Service
 * @see ServiceStrategies
 * 
 * @author rob
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Start {

}
