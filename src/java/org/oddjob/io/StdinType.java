package org.oddjob.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description Provide an output to the console.
 * 
 * @oddjob.example
 * 
 * Copy from stdin to a file.
 * 
 * {@oddjob.xml.resource org/oddjob/io/StdinTypeExample.xml}
 * 
 */
public class StdinType implements ValueFactory<InputStream> {

	public static final String NAME = "stdin";
	
	@Override
	public InputStream toValue() throws ArooaConversionException {
		return new FilterInputStream(System.in) {
			@Override
			public void close() throws IOException {
			}
		};
	}
	
	@Override
	public String toString() {
		return NAME;
	}
}
