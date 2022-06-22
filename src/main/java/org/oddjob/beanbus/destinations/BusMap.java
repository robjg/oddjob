package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;

import java.util.function.Function;

/**
 * @oddjob.description Apply a {@link Function} to beans in a Bean Bus.
 *
 * @oddjob.example Apply a function to double the price on a {@code Fruit} bean.
 *
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanTransformerExample.xml}
 *
 * @author rob
 *
 * @param <F> The accepting from type.
 * @param <T> The onward to type.
 */
public class BusMap<F, T> extends AbstractFilter<F, T>
implements BusFilter<F, T> {

	/**
	 * @oddjob.property
	 * @oddjob.description The function to apply to beans on the bus.
	 * @oddjob.required Yes.
	 */
	private Function<? super F, ? extends T> function;

	@Override
	protected T filter(F from) {
		return function.apply(from);
	}
	
		
	public Function<? super F, ? extends T> getFunction() {
		return function;
	}

	public void setFunction(Function<? super F, ? extends T> filter) {
		this.function = filter;
	}	
}
