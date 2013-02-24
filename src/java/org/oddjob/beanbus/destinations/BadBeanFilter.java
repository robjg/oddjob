package org.oddjob.beanbus.destinations;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.oddjob.arooa.life.Configured;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BadBeanTransfer;
import org.oddjob.beanbus.BusFilter;

/**
 * Something that will catch bad beans and pass them to a handler.
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
	
	private int badBeanCount;
	
	@Configured
	public void configure() {
		badBeanCount = 0;
	}
	
	@Override
	public boolean add(T bean) {
		
		try {
			to.add(bean);
		}
		catch (IllegalArgumentException e) {
			if (badBeanHandler == null) {
				if (badBeanCount == 0) {
					logger.info("No Bad Bean Handler. Bad Beans will be ignored.");
				}
			}
			else {
				badBeanHandler.add(new BadBeanTransfer<T>(bean, e));
			}
			++badBeanCount;
		}
		
		return true;
	};
	
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

	public int getBadBeanCount() {
		return badBeanCount;
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
}
