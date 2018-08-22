package org.oddjob.beanbus;

/**
 * Marker interface for things that can be part of a bean bus and drive
 * beans down the bus.
 * <p>
 * It is not required to implement this interface to be a driver on a 
 * bean bus. 
 * 
 * @author rob
 *
 * @param <T>
 */
public interface BusDriver<T> extends Runnable, Outbound<T> {

}
