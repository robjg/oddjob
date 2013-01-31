package org.oddjob.beanbus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.oddjob.arooa.types.ValueFactory;

/**
 * A {@link Destination} that collects beans in a list.
 * 
 * @author rob
 *
 * @param <T> The type of the beans to be collected.
 */
public class BeanTrap<T> implements Destination<T>, 
		Iterable<T>, 
		ValueFactory<List<T>>,
		BusAware {

	private final List<T> trapped = new ArrayList<T>();
	
	public void accept(T bean) {
		trapped.add(bean);
	};
	
	@Override
	public void setBeanBus(BusConductor driver) {
		driver.addBusListener(new BusListenerAdapter() {
			@Override
			public void busStarting(BusEvent event) {
				trapped.clear();
			}

			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
			}
		});
	}
	
	@Override
	public Iterator<T> iterator() {
		return toValue().iterator();
	}
				
	@Override
	public List<T> toValue() {
		return new ArrayList<T>(trapped);
	}
}
