package org.oddjob;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.Service;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class OddjobComponentResolverTest {

    @Test
    public void testRunnable() {

        OddjobComponentResolver test =
                new OddjobComponentResolver();

        Runnable runnable = () -> {
        };

        Object proxy = test.resolve(runnable, new StandardArooaSession());

        assertThat(proxy, instanceOf(Runnable.class));
    }

    public static class OurService implements Service {

        public void start() {
        }

        public void stop() {
        }
    }


    @Test
    public void testService() {

        OddjobComponentResolver test =
                new OddjobComponentResolver();

        Object proxy = test.resolve(new OurService(),
                new MockArooaSession());

        assertThat(proxy, instanceOf(Runnable.class));

        // TODO: It probably shouldn't do this
        assertThat(proxy, instanceOf(Service.class));
    }

    public static class OurRunnableService implements Runnable, Closeable {

        @Start
        @Override
        public void run() {

        }

        @Stop
        @Override
        public void close() throws IOException {

        }
    }

    @Test
    public void testThatRunnableServiceIsAServiceTo() throws FailedToStopException {

        OddjobComponentResolver test =
                new OddjobComponentResolver();

        Object proxy = test.resolve(new OurRunnableService(),
                new StandardArooaSession());

        assertThat(proxy, instanceOf(Runnable.class));

        StateSteps stateSteps = new StateSteps((Stateful) proxy);
        stateSteps.startCheck(ServiceState.STARTABLE, ServiceState.STARTING, ServiceState.STARTED);

        ((Runnable) proxy).run();

        stateSteps.checkNow();

        stateSteps.startCheck(ServiceState.STARTED, ServiceState.STOPPED);

        ((Stoppable) proxy).stop();

        stateSteps.checkNow();
    }


    static class OurSerializableRunnable implements Runnable, Serializable {
        private static final long serialVersionUID = 2009011000L;

        String colour = "red";

        public void run() {
        }
    }

    @Test
    public void testRestore() throws IOException, ClassNotFoundException {

        ArooaSession arooaSession = new StandardArooaSession();

        OddjobComponentResolver test =
                new OddjobComponentResolver();

        OurSerializableRunnable job = new OurSerializableRunnable();

        Object proxy = test.resolve(job, arooaSession);

        Object restoredProxy = OddjobTestHelper.copy(proxy);

        Object restoredJob = test.restore(restoredProxy,
                arooaSession);

        assertThat(OurSerializableRunnable.class, Matchers.sameInstance(restoredJob.getClass()));

        assertThat(job.colour, is("red"));
    }
}
