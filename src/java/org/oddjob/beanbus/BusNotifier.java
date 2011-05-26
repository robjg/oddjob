package org.oddjob.beanbus;

public interface BusNotifier {

	public void addBusListener(BusListener listener);
	
	public void removeBusListener(BusListener listener);
}
