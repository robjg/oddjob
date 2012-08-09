package org.oddjob.script;

import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;

/**
 * Provide {@link InvokerArguments} that will be converted using
 * an {@link ArooaConverter}.
 * 
 * @author rob
 *
 */
public class ConvertableArguments implements InvokerArguments {

	private final ArooaConverter converter;
	
	private final Object[] args;
	
	
	public ConvertableArguments(ArooaConverter converter, 
			Object... args) {
		this.converter = converter;
		this.args = args;
	}
	
	@Override
	public int size() {
		return args.length;
	}
	
	@Override
	public <T> T getArgument(int index, Class<T> type) 
	throws NoConversionAvailableException, ConversionFailedException {
		return converter.convert(args[index], type);
	}
	
}
