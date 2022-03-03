package org.oddjob.beanbus.mega;

import org.oddjob.Stateful;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.state.State;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper class used to decide when to Crash or Stop a bus by using the
 * state of the components in the bus.
 *
 * @author rob
 */
public class StatefulBusSupervisor {

    private static final Logger logger =
            LoggerFactory.getLogger(StatefulBusSupervisor.class);

    private final BusControls busControls;

    class SupervisorStateListener implements StateListener {

        // Only crash or stop the bus once.
        private final AtomicBoolean sent = new AtomicBoolean();

        @Override
        public void jobStateChange(StateEvent event) {
            State state = event.getState();
            if (StateConditions.ENDED.test(event.getState())) {
                Stateful stateful = event.getSource();
                stateful.removeStateListener(this);

                if (sent.compareAndSet(false, true)) {
                    if (StateConditions.EXCEPTION.test(event.getState())) {
                        logger.debug("Received {}, crashing bus.", event);
                        busControls.crashBus(event.getException());
                    } else {
                        logger.debug("Received {}, stopping bus.", event);
                        busControls.stopBus();
                    }
                }
            }
        }
    }

    public StatefulBusSupervisor(BusControls busControls, Executor executor) {
        this.busControls = Objects.requireNonNull(busControls);
    }

    public void supervise(Object... components) throws BusCrashException {

        SupervisorStateListener listener = new SupervisorStateListener();

        for (Object component: components) {
            if (component instanceof Stateful) {
                Stateful stateful = (Stateful) component;

                stateful.addStateListener(listener);
            }
        }
    }
}
