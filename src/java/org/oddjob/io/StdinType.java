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
 * <code><pre>
 * &lt;copy&gt;
 *  &lt;input&gt;
 *   &lt;stdin/&gt;
 *  &lt;/input&gt;
 *  &lt;output&gt;
 *   &lt;file file="foo.txt"/&gt;
 *  &lt;/output&gt;
 * &lt;/copy&gt;
 * </pre></code>
 * 
 */
public class StdinType implements ValueFactory<InputStream> {

	@Override
	public InputStream toValue() throws ArooaConversionException {
		return new FilterInputStream(System.in) {
			@Override
			public void close() throws IOException {
			}
		};
	}
}
