package org.oddjob.beanbus.bus;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.beanbus.Outbound;
import org.oddjob.beanbus.example.Fruit;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import javax.inject.Inject;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;

public class FlushingTest {

    public static class FlushControl implements Consumer<String>, Outbound<String> {

        Flushable flusher;

        Consumer<? super String> destination;

        @Inject
        public void setFlusher(Flushable flusher) {
            this.flusher = flusher;
        }

        @Override
        public void setTo(Consumer<? super String> destination) {
            this.destination = destination;
        }

        @Override
        public void accept(String s) {
            if ("flush".equals(s)) {
                try {
                    flusher.flush();
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            else {
                destination.accept(s);
            }
        }
    }

    public static class FlushListener implements Flushable, Consumer<String>, Outbound<String> {

        Consumer<? super String> destination;

        @Override
        public void flush() throws IOException {
            destination.accept("I'm Flushed!");
        }

        @Override
        public void accept(String s) {
            destination.accept(s);
        }

        @Override
        public void setTo(Consumer<? super String> destination) {
            this.destination = destination;
        }
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testFlushExample() throws ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("FlushExample.xml")).getFile()));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.COMPLETE);

        oddjob.run();

        states.checkNow();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<Fruit> results = lookup.lookup(
                "results.beans", List.class);

        assertThat(results, Matchers.contains("Apple", "I'm Flushed!", "Pear", "I'm Flushed!"));

        Object beanBus = lookup.lookup("beanBus");

        ((Resettable) beanBus).hardReset();
        ((Runnable) beanBus).run();

        results = lookup.lookup(
                "results.beans", List.class);

        assertThat(results, Matchers.contains("Apple", "I'm Flushed!", "Pear", "I'm Flushed!"));

        oddjob.destroy();
    }

}
