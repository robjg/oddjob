package org.oddjob.framework.adapt.service;

import org.apache.commons.beanutils.PropertyUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.StateSteps;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AsyncServiceTest {

    public interface Completable {
        void complete();
    }

    public static class MyService implements Completable {
        boolean stopped;

        CompletableFuture<Void> cf;

        @Start
        public CompletableFuture<Void> start() {
            cf = new CompletableFuture<>();
            return cf;
        }

        @Stop
        public void stop() {
            stopped = true;
        }

        public boolean isStopped() {
            return stopped;
        }

        @Override
        public void complete() {
            cf.complete(null);
        }
    }

    @Test
    public void testInOddjob() throws Exception {

        String xml = "<oddjob>" +
                " <job>" +
                "  <bean class='" + MyService.class.getName() + "' id='s' />" +
                " </job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        Object test = new OddjobLookup(oddjob).lookup("s");
        assertThat(((Stateful) test).lastStateEvent().getState(), is(ServiceState.INITIALISING));

        ((Completable) test).complete();

        assertThat(((Stateful) test).lastStateEvent().getState(), is(ServiceState.STARTED));

        oddjob.stop();

        assertThat(((Stateful) test).lastStateEvent().getState(), is(ServiceState.STOPPED));

        assertThat(PropertyUtils.getProperty(test, "stopped"), is(true));

        oddjob.destroy();
    }

    public static class ImmediatelyBadService {

        @Start
        public CompletableFuture<Void> start() {
            throw new IllegalStateException("I'm bad");
        }

        @Stop
        public void stop() {
            throw new RuntimeException("Unexpected!");
        }
    }


    @Test
    public void testImmediatelyBadInOddjob() {

        String xml = "<oddjob>" +
                " <job>" +
                "  <bean class='" + ImmediatelyBadService.class.getName() + "' id='s' />" +
                " </job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXCEPTION));

        Throwable childException = oddjob.lastStateEvent().getException().getCause();

        assertThat(childException, Matchers.instanceOf(InvocationTargetException.class));

        Throwable actualException = ((InvocationTargetException) childException).getTargetException();

        assertThat(actualException.getMessage(), is("I'm bad"));

        oddjob.destroy();
    }

    public static class TurnsBadService implements Completable {

        BlockingQueue<RuntimeException> blockingQueue = new LinkedBlockingQueue<>();
        @Start
        public CompletableFuture<Void> start() {
            return CompletableFuture.supplyAsync(
                    () -> {
                        try {
                            throw Objects.requireNonNull(
                                    blockingQueue.poll(5, TimeUnit.SECONDS));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        @Stop
        public void stop() {
            throw new RuntimeException("Unexpected!");
        }

        @Override
        public void complete() {
            blockingQueue.add(new IllegalStateException("I'm bad"));
        }
    }

    @Test
    public void testTurnsBadInOddjob() throws InterruptedException {

        String xml = "<oddjob>" +
                " <job>" +
                "  <bean class='" + TurnsBadService.class.getName() + "' id='s' />" +
                " </job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.ACTIVE));


        Object test = new OddjobLookup(oddjob).lookup("s");
        assertThat(((Stateful) test).lastStateEvent().getState(), is(ServiceState.INITIALISING));

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck(ParentState.ACTIVE, ParentState.EXCEPTION);

        ((Completable) test).complete();

        oddjobState.checkWait();

        Throwable childException = oddjob.lastStateEvent().getException().getCause();

        assertThat(childException, Matchers.instanceOf(CompletionException.class));

        Throwable actualException = ((CompletionException) childException).getCause();

        assertThat(actualException.getMessage(), is("I'm bad"));

        oddjob.destroy();
    }
}
