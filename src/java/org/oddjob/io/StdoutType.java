package org.oddjob.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description Provide an input from the console.
 * 
 * @oddjob.example
 * 
 * Copy from a file to stdout.
 * 
 * <code><pre>
 * &lt;copy&gt;
 *  &lt;input&gt;
 *   &lt;file file="foo.txt"/&gt;
 *  &lt;/input&gt;
 *  &lt;output&gt;
 *   &lt;stdout/&gt;
 *  &lt;/output&gt;
 * &lt;/copy&gt;
 * </pre><code>
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
