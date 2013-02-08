package org.oddjob.beanbus;

import java.util.Collection;

public class FilterSection<F, T> extends AbstractDestination<F> 
implements Section<F, T>, BusAware {

	private Filter<? super F, ? extends T> filter;

	private Collection<? super T> to;
	
	@Override
	public boolean add(F bean) {

		T filtered = filter.filter(bean);
		
		if (filtered == null) {
			return false;
		}
		else {
			to.add(filtered);
			return true;
		}
		
	};
		
	public Filter<? super F, ? extends T> getFilter() {
		return filter;
	}

	public void setFilter(Filter<? super F, ? extends T> filter) {
		this.filter = filter;
	}

	public Collection<? super T> getTo() {
		return to;
	}

	public void setTo(Collection<? super T> receiver) {
		this.to = receiver;
	}
	
	@Override
	public void setBeanBus(BusConductor driver) {
		if (to instanceof BusAware) {
			((BusAware) to).setBeanBus(driver);
		}
	}
}
