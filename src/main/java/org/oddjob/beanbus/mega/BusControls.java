package org.oddjob.beanbus.mega;

import org.oddjob.beanbus.BusCrashException;

public interface BusControls {

    void stopBus();

    void crashBus(Throwable exception) throws BusCrashException;
}
