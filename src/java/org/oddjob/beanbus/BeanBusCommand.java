package org.oddjob.beanbus;

public interface BeanBusCommand {

	public void run() throws BusCrashException;
}
