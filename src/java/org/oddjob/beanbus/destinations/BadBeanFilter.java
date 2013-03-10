package org.oddjob.beanbus.destinations;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BadBeanTransfer;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.beanbus.TrackingBusListener;

/**
 * @oddjob.description Something that will catch bad beans and pass them to 
 * a handler.
 * 
 * @author rob
 *
 * @param <T>
 */
public class BadBeanFilter<T> extends AbstractDestination<T>
implements BusFilter<T, T> {

	private static final Logger logger = Logger.getLogger(BadBeanFilter.class);
	
	private Collection<? super BadBeanTransfer<T>> badBeanHandler;

	private Collection<? super T> to;
	
	private String name;
	
	private int badCount;
	
	private int count;
	
	private final TrackingBusListener trackingListener = 
			new TrackingBusListener() {
		@Override
		public void busStarting(BusEvent event) {
			badCount = 0;
			count = 0;
		}
	};
	
	@Override
	public boolean add(T bean) {
		
		if (to == null) {
			if (count == 0) {
				logger.info("No destination set. Beans will be ignored.");
			}
			return false;
		}
		
		try {
			to.add(bean);
			++count;
		}
		catch (RuntimeException e) {
			Throwable t = e;
			do {
				if (t instanceof IllegalArgumentException) {
					if (badBeanHandler == null) {
						if (badCount == 0) {
							logger.info("No Bad Bean Handler. Bad Beans will be ignored.");
						}
					}
					else {
						badBeanHandler.add(new BadBeanTransfer<T>(bean, 
								(IllegalArgumentException) t));
					}
					++badCount;
					++count;
				}
				t = t.getCause();
			} 
			while (t != null);
		}
		
		return true;
	};
	
	@ArooaHidden
	@Inject
	public void setBusConductor(BusConductor busConductor) {
		trackingListener.setBusConductor(busConductor);
	}	
	
	@Override
	public void setTo(Collection<? super T> to) {
		this.to = to;
	}
	
	public Collection<? super T> getTo() {
		return to;
	}
	
	public Collection<? super BadBeanTransfer<T>> getBadBeanHandler() {
		return badBeanHandler;
	}

	public void setBadBeanHandler(
			Collection<? super BadBeanTransfer<T>> badBeanHandler) {
		this.badBeanHandler = badBeanHandler;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBadCount() {
		return badCount;
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}

	public int getCount() {
		return count;
	}
}
