package org.oddjob.beanbus.destinations;

import org.oddjob.FailedToStopException;
import org.oddjob.beanbus.BadBeanTransfer;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.framework.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @oddjob.description Something that will catch bad beans and pass them to 
 * a handler.
 * 
 * @author rob
 *
 * @param <T>
 */
public class BadBeanFilter<T> implements BusFilter<T, T>, Service {

	private static final Logger logger = LoggerFactory.getLogger(BadBeanFilter.class);
	
	private Consumer<? super BadBeanTransfer<T>> badBeanHandler;

	private Consumer<? super T> to;
	
	private String name;
	
	private int badCount;
	
	private int count;


	@Override
	public void start() throws Exception {
		badCount = 0;
		count = 0;
	}

	@Override
	public void stop() throws FailedToStopException {

	}

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
					break;
				}
				t = t.getCause();
			} 
			while (t != null);
			if (t == null) {
				throw e;
			}
		}
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
