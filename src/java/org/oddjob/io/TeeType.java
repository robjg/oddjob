package org.oddjob.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.utils.ListSetterHelper;

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
public class TeeType implements ArooaValue, ArooaSessionAware {

	private volatile ArooaConverter converter;
	
	private volatile ArooaValue input;
	
	private final List<ArooaValue> outputs =
		new ArrayList<ArooaValue>();
		
	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
			
			registry.register(TeeType.class, InputStream.class, 
					new Convertlet<TeeType, InputStream>() {
				@Override
				public InputStream convert(TeeType from)
				throws ArooaConversionException {
					return from.toInputStream();
				}
			});
			
			registry.register(TeeType.class, OutputStream.class, 
					new Convertlet<TeeType, OutputStream>() {
				@Override
				public OutputStream convert(TeeType from)
				throws ArooaConversionException {
					return from.toOutputStream();
				}
			});
		}
	}
	
	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		this.converter = session.getTools().getArooaConverter();
	}
	
    /**
     * @oddjob.property input
     * @oddjob.description An input stream that will be copied to the outputs.
     * @oddjob.required Only if this type is required to be an input stream.
     */
	public void setInput(ArooaValue input) {
		this.input = input;
	}
	
    /**
     * @oddjob.property outputs
     * @oddjob.description List of outputs to split to.
     * @oddjob.required No, output will be thrown away if missing.
     */
	public void setOutputs(int index, ArooaValue output) {
		
		new ListSetterHelper<ArooaValue>(outputs).set(index, output);
	}
	
	public InputStream toInputStream() throws NoConversionAvailableException, ConversionFailedException {
		
		if (input == null) {
			return null;
		}
		
		InputStream input = converter.convert(this.input, InputStream.class);
		
		return new WireTapInputStream(input, toOutputStream());
	}
	
	public OutputStream toOutputStream() throws NoConversionAvailableException, ConversionFailedException {

		final List<OutputStream> outputs =
				new ArrayList<>();
		
		for (ArooaValue value : this.outputs) {
			outputs.add(converter.convert(value, OutputStream.class));
		}
		
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

			@Override
			public String toString() {
				return "TeeOutputStream to " + outputs.size() + 
						" OutputStreams";
			}
		};
	}
	
	
}
