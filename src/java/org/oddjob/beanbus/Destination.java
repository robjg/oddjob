package org.oddjob.beanbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.oddjob.beanbus.mega.OutboundStrategies;

/**
 * Annotate that a method is to have the destination set for an
 * {@link Outbound}.
 * 
 * @see OutboundStrategies
 * 
 * @author rob
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Destination {

}
