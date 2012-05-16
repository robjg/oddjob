package org.oddjob.beanbus;

/**
 * Definition of the idea of a bean bus.
 * <p>
 * A bean bus is something that uses a {@link Driver} to take
 * Beans to a {@link Destination}.
 * 
 * @author rob
 *
 */
public interface BeanBus extends Runnable, BusNotifier, StageNotifier {

	public void stop();
}
