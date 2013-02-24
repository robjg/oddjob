package org.oddjob.beanbus.destinations;

import javax.inject.Inject;

import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListenerAdapter;

public class OnlyFilter<F> extends AbstractFilter<F, F>{

	private int only;
	
	private int beanCount;
	
	private boolean stopBus;
	
	private BusConductor busConductor;
	
	@Override
	protected F filter(F from) {
		++beanCount;
		++only;
		
		if (beanCount > only) {
			if (stopBus) {
				busConductor.requestBusStop();
			}
			return null;
		}
		else {
			return from;
		}
	}

	public int getOnly() {
		return only;
	}

	public void setOnly(int only) {
		this.only = only;
	}

	public int getBeanCount() {
		return beanCount;
	}
	
	@Inject
	public void setBusConductor(BusConductor busConductor) {

		this.busConductor = busConductor;
		
		busConductor.addBusListener(new BusListenerAdapter() {
			@Override
			public void busStarting(BusEvent event) throws BusCrashException {
				beanCount = 0;
			}
		});
	}

	public boolean isStopBus() {
		return stopBus;
	}

	public void setStopBus(boolean stopBus) {
		this.stopBus = stopBus;
	}
}
