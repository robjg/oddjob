package org.oddjob.beanbus;

/**
 * Something that will catch bad beans and pass them to a handler.
 * 
 * @author rob
 *
 * @param <T>
 */
public class BadBeanFilter<T> implements Section<T, T>, BusAware {

	private BadBeanHandler<? super T> badBeanHandler;

	private Destination<? super T> to;
	
	public void accept(T bean) 
	throws BusCrashException {
		
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
	public void setBeanBus(BusConductor driver) {
		driver.addBusListener(new BusListenerAdapter() {
			@Override
			public void busStarting(BusEvent event) throws BusCrashException {
				if (badBeanHandler == null) {
					throw new BusCrashException("No Bad Bean Handler.");
				}
	
			}
			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
			}
		});
	}
	
	public BadBeanHandler<? super T> getBadBeanHandler() {
		return badBeanHandler;
	}

	public void setBadBeanHandler(BadBeanHandler<? super T> badBeanHandler) {
		this.badBeanHandler = badBeanHandler;
	}
}
