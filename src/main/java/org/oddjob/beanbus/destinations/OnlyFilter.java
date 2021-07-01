package org.oddjob.beanbus.destinations;

import org.oddjob.FailedToStopException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.framework.Service;

import javax.inject.Inject;

/**
 * Only allow a certain number of beans passed.
 * 
 * @author rob
 *
 * @param <F>
 */
public class OnlyFilter<F> extends AbstractFilter<F, F> implements Service {

	private int only;
	
	private int count;
	
	private boolean stopBus;
	
	private BusConductor busConductor;

	@Override
	public void stop() throws FailedToStopException {

	}

	@Override
	public void start() throws Exception {
		count = 0;
	}

	@Override
	protected F filter(F from) {
		++count;
		
		if (count > only) {
			if (stopBus) {
				busConductor.close();
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
	}

	public boolean isStopBus() {
		return stopBus;
	}

	public void setStopBus(boolean stopBus) {
		this.stopBus = stopBus;
	}
}
