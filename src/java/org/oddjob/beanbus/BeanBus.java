package org.oddjob.beanbus;

public interface BeanBus extends Runnable, BusNotifier, StageNotifier {

	public void stop();
}
