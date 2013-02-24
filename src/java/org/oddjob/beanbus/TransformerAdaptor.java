package org.oddjob.beanbus;

/**
 * An adaptor from an {@link Transformer} to a {@link BusFilter}.
 * 
 * @author rob
 *
 * @param <F>
 * @param <T>
 */
public class TransformerAdaptor<F, T> extends AbstractFilter<F, T> 
implements BusFilter<F, T> {

	private Transformer<? super F, ? extends T> transformer;

	@Override
	protected T filter(F from) {
		return transformer.transform(from);
	}
	
		
	public Transformer<? super F, ? extends T> getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer<? super F, ? extends T> filter) {
		this.transformer = filter;
	}	
}
