package org.oddjob.beanbus.bus;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobMatchers;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.beanbus.Destination;
import org.oddjob.framework.extend.SimpleService;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BeanBusTest {

    static class OurService extends SimpleService {

        final AtomicInteger stopCount = new AtomicInteger();

        @Override
        protected void onStart() {

        }

        @Override
        protected void onStop() throws FailedToStopException {
            super.onStop();

            stopCount.incrementAndGet();
        }
    }

    @Test
    void whenSingleServiceThenBusStateStarted() throws FailedToStopException {

        BasicBusService basicBusService = new BasicBusService();

        OurService ourService = new OurService();

        basicBusService.setOf(0, ourService);

        basicBusService.run();

        assertThat(basicBusService,
                OddjobMatchers.statefulIs(StateConditions.STARTED));

        ourService.stop();

        assertThat(basicBusService,
                OddjobMatchers.statefulIs(StateConditions.COMPLETE));

        assertThat(ourService.stopCount.get(), is(1));
    }

    @Test
    public void testServiceStoppedMidStart() throws InterruptedException, FailedToStopException {

        BasicBusService basicBusService = new BasicBusService();

        OurService ourService = new OurService();

        WaitJob waitJob = new WaitJob();

        basicBusService.setOf(0, waitJob);
        basicBusService.setOf(1, ourService);

        StateSteps waitState = new StateSteps(waitJob);
        waitState.startCheck(StateConditions.READY, StateConditions.EXECUTING);

        Thread t = new Thread(basicBusService);
        t.start();

        waitState.checkWait();

        ourService.stop();

        t.join(5000L);

        assertThat(basicBusService,
                OddjobMatchers.statefulIs(StateConditions.COMPLETE));
    }

    public static class AddTwo implements Consumer<Integer> {

        private Consumer<Integer> next;

        @Override
        public void accept(Integer integer) {
            next.accept(integer + 2);
        }

        public Consumer<Integer> getNext() {
            return next;
        }

        @Destination
        public void setNext(Consumer<Integer> next) {
            this.next = next;
        }
    }

    @Test
    public void whenConsumerWithDestinationThenWrapped() throws ArooaConversionException, FailedToStopException {

        File file = new File(Objects.requireNonNull(
                getClass().getResource("ServiceBusExample.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        oddjob.run();

        assertThat(oddjob, OddjobMatchers.statefulIs(StateConditions.STARTED));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        @SuppressWarnings("unchecked")
        Consumer<Object> consumer = lookup.lookup("bus", Consumer.class);

        consumer.accept(2);

        List<?> addTwoResults = lookup.lookup("addTwo.next.values", List.class);
        assertThat(addTwoResults, Matchers.contains(4));

        List<?> busResults = lookup.lookup("bus.to.values", List.class);
        assertThat(busResults, Matchers.contains(2));

        oddjob.stop();

        OddjobMatchers.statefulIs(StateConditions.COMPLETE);
    }
}
