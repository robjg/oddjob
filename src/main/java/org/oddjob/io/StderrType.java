package org.oddjob.io;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * @oddjob.description Provide an output to the stderr stream of 
 * the console.
 * 
 * @oddjob.example
 * 
 * Copy a buffer to stderr.
 * 
 * {@oddjob.xml.resource org/oddjob/io/StderrTypeExample.xml}
 */
public class StderrType implements ArooaValue {

	public static final String NAME = "stderr";

	public static class Conversions implements ConversionProvider {

		public void registerWith(ConversionRegistry registry) {

			registry.register(StderrType.class, OutputStream.class,
					StderrType::toOutputStream);

			registry.register(StderrType.class, Consumer.class,
					StderrType::toConsumer);
		}
	}

	public OutputStream toOutputStream() {
		return new NoCloseOutputStream(System.err, NAME);
	}

	public Consumer<Object> toConsumer() {

		PrintStream printStream = System.err;

		return printStream::println;
	}

	@Override
	public String toString() {
		return NAME;
	}
}
