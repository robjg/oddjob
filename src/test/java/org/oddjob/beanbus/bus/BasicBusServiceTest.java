package org.oddjob.beanbus.bus;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.Outbound;
import org.oddjob.framework.Service;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class BasicBusServiceTest {

    public static class OurService implements Flushable, Service, Consumer<String>, Outbound<String> {

        private Consumer<? super String> consumer;

        private String last;

        @Override
        public void accept(String s) {
            last = s;
        }

        @Override
        public void flush() throws IOException {
            consumer.accept(last);
        }

        @Override
        public void stop() throws FailedToStopException {

        }

        @Override
        public void start() throws Exception {

        }

        @Override
        public void setTo(Consumer<? super String> destination) {
            this.consumer = destination;
        }
    }

    @Test
    void testFlush() throws ArooaConversionException, FailedToStopException {

        String config =
                "<oddjob>" +
                        "<job>" +
                        "   <bus:bus id=\"bean-bus\" xmlns:bus=\"oddjob:beanbus\">" +
                        "     <of>" +
                        "        <bean class='" + OurService.class.getName() + "' id='service'/>" +
                        "        <bus:collect id='results'/>" +
                        "     </of>" +
                        "   </bus:bus>" +
                        "</job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", config));

        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        BasicBusService bus = lookup.lookup("bean-bus", BasicBusService.class);

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.STARTED));

        bus.accept("apple");

        List<?> results  = lookup.lookup("results.list", List.class);

        assertThat(results, Matchers.empty());

        bus.flush();

        results = lookup.lookup("results.list", List.class);

        assertThat(results, contains("apple"));

        oddjob.stop();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        oddjob.destroy();
    }

    @Test
    void filterExample() throws InterruptedException {

        File file = new File(Objects.requireNonNull(
                getClass().getResource("FilterExample.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(StateConditions.READY, StateConditions.EXECUTING,
                StateConditions.ACTIVE, StateConditions.COMPLETE);

        oddjob.run();

        states.checkWait();

        oddjob.destroy();
    }
}