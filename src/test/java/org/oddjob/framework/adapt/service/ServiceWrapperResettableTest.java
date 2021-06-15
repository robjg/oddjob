package org.oddjob.framework.adapt.service;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.adapt.HardReset;
import org.oddjob.framework.adapt.SoftReset;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.StateSteps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ServiceWrapperResettableTest {

    public static class OurService {

        boolean reset;

        @Start
        public void start() {}

        @Stop
        public void stop() {}

        @HardReset
        @SoftReset
        public void reset() {
            reset = true;
        }
    }

    @Test
    public void testResettable() throws FailedToStopException {

        ArooaSession session = new StandardArooaSession();

        OurService wrapped = new OurService();

        ServiceAdaptor serviceAdaptor = new ServiceStrategies()
                .serviceFor(wrapped, session);

        Runnable wrapper = (Runnable) new ServiceProxyGenerator().generate(
                serviceAdaptor, getClass().getClassLoader());

        ((ArooaSessionAware) wrapper).setArooaSession(session);

        StateSteps states = new StateSteps((Stateful) wrapper);
        states.startCheck(ServiceState.STARTABLE, ServiceState.STARTING, ServiceState.STARTED,
                ServiceState.STOPPED);

        wrapper.run();
        ((Stoppable) wrapper).stop();

        states.checkNow();

        states.startCheck(ServiceState.STOPPED);

        ((Resettable) wrapper).softReset();

        states.checkNow();

        assertThat(wrapped.reset, is(false));

        states.startCheck(ServiceState.STOPPED, ServiceState.STARTABLE);

        ((Resettable) wrapper).hardReset();

        states.checkNow();

        assertThat(wrapped.reset, is(true));
    }
}
