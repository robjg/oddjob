package org.oddjob.framework.adapt;

import org.oddjob.Resettable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate that a method is to be called during a soft reset.
 * 
 * @see Resettable
 * @see ResetableAdaptorFactory
 * 
 * @author rob
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SoftReset {

}
