package org.oddjob.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description Provide an output to stdout stream of 
 * the console.
 * 
 * @oddjob.example
 * 
 * Copy from a file to stdout.
 * 
 * {@oddjob.xml.resource org/oddjob/io/StdoutTypeExample.xml}
 * 
 */
public class StdoutType implements ValueFactory<OutputStream> {

	@Override
	public OutputStream toValue() throws ArooaConversionException {
		return new FilterOutputStream(System.out) {
			@Override
			public void close() throws IOException {
				super.flush();
			}
		};
	}
}
