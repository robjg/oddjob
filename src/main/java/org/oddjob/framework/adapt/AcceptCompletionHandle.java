package org.oddjob.framework.adapt;

import org.oddjob.framework.AsyncJob;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate that a method can accept a {@link Runnable} for a stop handle.
 * 
 * @see AsyncJob
 *
 * @author rob
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AcceptCompletionHandle {

}
