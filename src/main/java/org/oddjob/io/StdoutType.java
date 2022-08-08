package org.oddjob.io;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * @oddjob.description Provide an output to stdout stream of
 * the console.
 * @oddjob.example Copy from a file to stdout.
 * <p>
 * {@oddjob.xml.resource org/oddjob/io/StdoutTypeExample.xml}
 */
public class StdoutType implements ArooaValue {

    public static final String NAME = "stdout";

    public static class Conversions implements ConversionProvider {

        public void registerWith(ConversionRegistry registry) {

            registry.register(StdoutType.class, OutputStream.class,
                    StdoutType::toOutputStream);

            registry.register(StdoutType.class, Consumer.class,
                    StdoutType::toConsumer);
        }
    }

    public OutputStream toOutputStream() {
        return new NoCloseOutputStream(System.out, NAME);
    }

    public Consumer<Object> toConsumer() {

        PrintStream printStream = System.out;

        return printStream::println;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
