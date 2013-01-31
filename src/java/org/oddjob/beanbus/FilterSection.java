package org.oddjob.beanbus;

public class FilterSection<F, T> implements Section<F, T>, BusAware {

	private Filter<? super F, ? extends T> filter;

	private Destination<? super T> to;
	
	public void accept(F bean) throws BadBeanException, BusCrashException {

		T filtered = filter.filter(bean);
		
		if (filtered == null) {
			return;
		}
		
		to.accept(filtered);
	};
		
	public Filter<? super F, ? extends T> getFilter() {
		return filter;
	}

	public void setFilter(Filter<? super F, ? extends T> filter) {
		this.filter = filter;
	}

	public Destination<? super T> getTo() {
		return to;
	}

	public void setTo(Destination<? super T> receiver) {
		this.to = receiver;
	}
	
	@Override
	public void setBeanBus(BusConductor driver) {
		if (to instanceof BusAware) {
			((BusAware) to).setBeanBus(driver);
		}
	}
}
