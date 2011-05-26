package org.oddjob.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description Provide an output to the stderr stream of 
 * the console.
 * 
 * @oddjob.example
 * 
 * Copy a buffer to stderr.
 * 
 * <pre><code>
 * &lt;copy&gt;
 *  &lt;input&gt;
 *   &lt;buffer&gt;It's all going wrong!&lt;/buffer&gt;
 *  &lt;/input&gt;
 *  &lt;output&gt;
 *   &lt;stderr/&gt;
 *  &lt;/output&gt;
 * &lt;/copy&gt;
 * </code></pre>
 */
public class StderrType implements ValueFactory<OutputStream> {

	@Override
	public OutputStream toValue() throws ArooaConversionException {
		return new FilterOutputStream(System.err) {
			@Override
			public void close() throws IOException {
				super.flush();
			}
		};
	}
}
