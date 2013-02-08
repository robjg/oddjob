package org.oddjob.beanbus;

import java.util.Collection;

/**
 * Something that will catch bad beans and pass them to a handler.
 * 
 * @author rob
 *
 * @param <T>
 */
public class BadBeanFilter<T> extends AbstractDestination<T>
implements Section<T, T>, BusAware {

	private Collection<? super BadBeanTransfer<T>> badBeanHandler;

	private Collection<? super T> to;
	
	@Override
	public boolean add(T bean) {
		
		try {
			to.add(bean);
		}
		catch (IllegalArgumentException e) {
			badBeanHandler.add(new BadBeanTransfer<T>(bean, e));
		}
		
		return true;
	};
	
	@Override
	public void setTo(Collection<? super T> to) {
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
	
	public Collection<? super BadBeanTransfer<T>> getBadBeanHandler() {
		return badBeanHandler;
	}

	public void setBadBeanHandler(
			Collection<? super BadBeanTransfer<T>> badBeanHandler) {
		this.badBeanHandler = badBeanHandler;
	}
}
