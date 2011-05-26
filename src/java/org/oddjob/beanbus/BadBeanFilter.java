package org.oddjob.beanbus;

public class BadBeanFilter<T> implements Section<T, T>, BusAware {

	private BadBeanHandler<? super T> badBeanHandler;

	private Destination<? super T> to;
	
	public void accept(T bean) 
	throws CrashBusException {
		
		try {
			to.accept(bean);
		}
		catch (BadBeanException e) {
			badBeanHandler.handle(bean, e);
		}
	};
	
	@Override
	public void setTo(Destination<? super T> to) {
		this.to = to;
	}
	
	@Override
	public void setBus(BeanBus driver) {
		driver.addBusListener(new BusListener() {
			@Override
			public void busStarting(BusEvent event) throws CrashBusException {
				if (badBeanHandler == null) {
					throw new CrashBusException("No Bad Bean Handler.");
				}
	
			}
			@Override
			public void busStopping(BusEvent event) throws CrashBusException {
			}
			@Override
			public void busCrashed(BusEvent event, BusException e) {
			}
			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
			}
		});
		if (badBeanHandler instanceof BusAware) {
			((BusAware) badBeanHandler).setBus(driver);
		}
		if (to instanceof BusAware) {
			((BusAware) to).setBus(driver);
		}
	}
	
	public BadBeanHandler<? super T> getBadBeanHandler() {
		return badBeanHandler;
	}

	public void setBadBeanHandler(BadBeanHandler<? super T> badBeanHandler) {
		this.badBeanHandler = badBeanHandler;
	}
	

}
