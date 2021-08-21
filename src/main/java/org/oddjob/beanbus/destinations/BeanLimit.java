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
public class BeanLimit<F> extends AbstractFilter<F, F> implements Service {

	private int limit;
	
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
		
		if (count > limit) {
			if (stopBus) {
				busConductor.close();
			}
			return null;
		}
		else {
			return from;
		}
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
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
