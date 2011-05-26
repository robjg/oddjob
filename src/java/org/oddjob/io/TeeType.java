package org.oddjob.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description Split output to multiple other outputs.
 * 
 * @oddjob.example
 * 
 * Copy a buffer to stdout and to a file.
 * 
 * <code><pre>
 * &lt;copy&gt;
 *  &lt;input&gt;
 *   &lt;buffer&gt;Duplicate This!&lt;/buffer&gt;
 *  &lt;/input&gt;
 *  &lt;output&gt;
 *   &lt;tee&gt;
 *    &lt;outputs&gt;
 *     &lt;stdout/&gt;
 *     &lt;file file='something.txt'/&gt;
 *    &lt;/outputs&gt;
 *   &lt;/tee&gt;
 *  &lt;/output&gt;
 * &lt;/copy&gt;
 * </pre></code>
 */
public class TeeType implements ValueFactory<OutputStream> {

	private final List<OutputStream> outputs =
		new ArrayList<OutputStream>();
		
    /**
     * @oddjob.property outputs
     * @oddjob.description List of outputs to split to.
     * @oddjob.required No, output will be thrown away if missing.
     */
	public void setOutputs(int index, OutputStream output) {
		
		if (output == null) {
			outputs.remove(index);
		}
		else {
			outputs.add(index, output);
		}
	}
	
	@Override
	public OutputStream toValue() throws ArooaConversionException {

		return new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				for (OutputStream output : outputs) {
					output.write(b);
				}
			}
			
			@Override
			public void write(byte[] b) throws IOException {
				for (OutputStream output : outputs) {
					output.write(b);
				}
			}
			
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				for (OutputStream output : outputs) {
					output.write(b, off, len);
				}
			}
			
			@Override
			public void flush() throws IOException {
				for (OutputStream output : outputs) {
					output.flush();
				}
			}
			
			@Override
			public void close() throws IOException {
				for (OutputStream output : outputs) {
					output.close();
				}
			}

		};
	}}
