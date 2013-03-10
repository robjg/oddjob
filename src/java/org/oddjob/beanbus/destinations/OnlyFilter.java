package org.oddjob.beanbus.destinations;

import javax.inject.Inject;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.TrackingBusListener;

/**
 * Only allow a certain number of beans passed.
 * 
 * @author rob
 *
 * @param <F>
 */
public class OnlyFilter<F> extends AbstractFilter<F, F>{

	private int only;
	
	private int count;
	
	private boolean stopBus;
	
	private BusConductor busConductor;
	
	private final TrackingBusListener busListener = 
			new TrackingBusListener() {
		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			count = 0;
		}
	};
	
	@Override
	protected F filter(F from) {
		++count;
		
		if (count > only) {
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

	public int getCount() {
		return count;
	}
	
	@ArooaHidden
	@Inject
	public void setBusConductor(BusConductor busConductor) {

		this.busConductor = busConductor;
		busListener.setBusConductor(busConductor);
	}

	public boolean isStopBus() {
		return stopBus;
	}

	public void setStopBus(boolean stopBus) {
		this.stopBus = stopBus;
	}
}
