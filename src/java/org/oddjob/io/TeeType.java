package org.oddjob.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

/**
 * @oddjob.description Split output to multiple other outputs.
 * 
 * @oddjob.example
 * 
 * Copy a buffer to stdout, the log, and to a file.
 * 
 * {@oddjob.xml.resource org/oddjob/io/TeeTypeOutputStream.xml}
 * 
 * @oddjob.example
 * 
 * Copy data to stdout as it is being read during a copy from one buffer to 
 * another.
 * 
 * {@oddjob.xml.resource org/oddjob/io/TeeTypeInputStream.xml}
 * 
 * 
 */
public class TeeType implements ArooaValue {

	private InputStream input;
	
	private final List<OutputStream> outputs =
		new ArrayList<OutputStream>();
		
	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
			
			registry.register(TeeType.class, InputStream.class, 
					new Convertlet<TeeType, InputStream>() {
				@Override
				public InputStream convert(TeeType from)
						throws ConvertletException {
					return from.toInputStream();
				}
			});
			
			registry.register(TeeType.class, OutputStream.class, 
					new Convertlet<TeeType, OutputStream>() {
				@Override
				public OutputStream convert(TeeType from)
						throws ConvertletException {
					return from.toOutputStream();
				}
			});
		}
	}
	
    /**
     * @oddjob.property input
     * @oddjob.description An input stream that will be copied to the outputs.
     * @oddjob.required Only if this type is required to be an input stream.
     */
	public void setInput(InputStream input) {
		this.input = input;
	}
	
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
	
	public InputStream toInputStream() {
		
		if (input == null) {
			return null;
		}
		
		return new WireTapInputStream(input, toOutputStream());
	}
	
	public OutputStream toOutputStream() {

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
	}
	
}
