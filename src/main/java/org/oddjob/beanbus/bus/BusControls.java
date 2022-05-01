package org.oddjob.beanbus.bus;

/**
 * Internal class passed from the Bus component to something that will decide when
 * it should stop or crash.
 *
 * @see StatefulBusSupervisor
 */
public interface BusControls {

    void stopBus();

    void crashBus(Throwable exception);
}
