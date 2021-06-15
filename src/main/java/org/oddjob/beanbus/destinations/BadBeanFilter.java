package org.oddjob.beanbus.destinations;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * @oddjob.description Something that will catch bad beans and pass them to 
 * a handler.
 * 
 * @author rob
 *
 * @param <T>
 */
public class BadBeanFilter<T> implements Consumer<T>, BusFilter<T, T> {

	private static final Logger logger = LoggerFactory.getLogger(BadBeanFilter.class);
	
	private Consumer<? super BadBeanTransfer<T>> badBeanHandler;

	private Consumer<? super T> to;
	
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
	public void accept(T bean) {
		
		if (to == null) {
			if (count == 0) {
				logger.info("No destination set. Beans will be ignored.");
			}
			return;
		}
		
		try {
			to.accept(bean);
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
						badBeanHandler.accept(new BadBeanTransfer<>(bean,
								(IllegalArgumentException) t));
					}
					++badCount;
					++count;
				}
				t = t.getCause();
			} 
			while (t != null);
		}
	}

	@ArooaHidden
	@Inject
	public void setBusConductor(BusConductor busConductor) {
		trackingListener.setBusConductor(busConductor);
	}	
	
	@Override
	public void setTo(Consumer<? super T> to) {
		this.to = to;
	}
	
	public Consumer<? super T> getTo() {
		return to;
	}
	
	public Consumer<? super BadBeanTransfer<T>> getBadBeanHandler() {
		return badBeanHandler;
	}

	public void setBadBeanHandler(
			Consumer<? super BadBeanTransfer<T>> badBeanHandler) {
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
