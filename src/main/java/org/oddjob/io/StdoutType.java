package org.oddjob.io;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

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
public class StdoutType implements ArooaValue {

	public static final String NAME = "stdout";

	public static class Conversions implements ConversionProvider {

		public void registerWith(ConversionRegistry registry) {

			registry.register(StdoutType.class, OutputStream.class,
					from -> from.toOutputStream());

			registry.register(StdoutType.class, Consumer.class,
					from -> from.toConsumer());
		}
	}

	public OutputStream toOutputStream() throws ArooaConversionException {
		return new FilterOutputStream(System.out) {
			@Override
			public void close() throws IOException {
				super.flush();
			}

			@Override
			public String toString() {
				return NAME;
			}
		};
	}

	public Consumer<Object> toConsumer() throws ArooaConversionException {

		OutputStream outputStream = toOutputStream();
		return o -> {
			try {
				outputStream.write(Objects.toString(o).getBytes(StandardCharsets.UTF_8));
				outputStream.write('\n');
				outputStream.flush();
			} catch (IOException e) {
				throw new IllegalStateException("Failed writing [" + o + "] to " + NAME);
			}
		};
	}

	@Override
	public String toString() {
		return NAME;
	}
}
