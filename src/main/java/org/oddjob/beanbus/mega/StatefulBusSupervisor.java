package org.oddjob.beanbus.mega;

import org.oddjob.Stateful;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.state.State;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adapts a {@link Stateful} into a {@Link BusConductor}.
 *
 * @author rob
 */
public class StatefulBusSupervisor {

    private static final Logger logger =
            LoggerFactory.getLogger(StatefulBusSupervisor.class);

    private final Executor executor;

    private BusActionExecutor actionExecutor;

    private final BusControls busControls;

    class BusDriverListener implements StateListener {

        private final Runnable shutdown;

        BusDriverListener(Runnable shutdown) {
            this.shutdown = shutdown;
        }

        @Override
        public void jobStateChange(StateEvent event) {
            State state = event.getState();

            if (StateConditions.ENDED.test(event.getState())) {
                shutdown.run();
                if (StateConditions.EXCEPTION.test(event.getState())) {
                    actionExecutor.crashBus(event.getException());
                } else {
                    actionExecutor.stopBus();
                }
            }
        }
    }

    class ComponentListener implements StateListener {

        @Override
        public void jobStateChange(StateEvent event) {
            State state = event.getState();

            if (StateConditions.EXCEPTION.test(event.getState())) {
                actionExecutor.crashBus(event.getException());
            }
        }
    }


    public StatefulBusSupervisor(BusControls busControls, Executor executor) {
        this.busControls = Objects.requireNonNull(busControls);
        this.executor = Objects.requireNonNull(executor);
    }

    public BusAction supervise(Object... components) throws BusCrashException {

        BusActionNow busActionNow = new BusActionNow();
        this.actionExecutor = busActionNow;

        int i = 0;

        Stateful driver = null;
        while (i < components.length) {
            Object component = components[i];
            ++i;
            if (component instanceof Stateful) {
                driver = (Stateful) component;
                break;
            }
        }

        if (driver == null) {
            throw new BusCrashException("No Bus Driver");
        } else {
            logger.info("Identified {} as the Bus Driver.", driver);
        }

        List<Runnable> shutdowns = new ArrayList<>();

        final Stateful fDriver = driver;
        final int fi = i;

        StateListener driverListener = new BusDriverListener(
                () -> shutdowns.forEach(Runnable::run));
        shutdowns.add(() -> fDriver.removeStateListener(driverListener));
        fDriver.addStateListener(driverListener);

        for (int j = fi; j < components.length; ++j) {
            Object component = components[j];
            if (component instanceof Stateful) {
                Stateful stateful = (Stateful) component;

                StateListener listener = new ComponentListener();
                stateful.addStateListener(listener);
                shutdowns.add(() -> stateful.removeStateListener(listener));
            }
        }

        if (busActionNow.exception != null) {
            throw busActionNow.exception;
        }

        return () -> {
            busActionNow.actionExecutor = this.executor;

            try {
                Optional<BusCrashException> maybeCrash = busActionNow.waitOnEnded();
                if (maybeCrash.isPresent()) {
                    throw maybeCrash.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    public interface BusAction {

        void run() throws BusCrashException;
    }

    public interface BusActionExecutor {

        void stopBus();

        void crashBus(Throwable t);
    }

    class BusActionNow implements BusActionExecutor {

        private final AtomicBoolean sent = new AtomicBoolean();

        private volatile BusCrashException exception;

        private final CountDownLatch latch = new CountDownLatch(1);

        private Executor actionExecutor = Runnable::run;

        @Override
        public void stopBus() {
            if (sent.compareAndSet(false, true)) {
                actionExecutor.execute(() -> {
                    try {
                        busControls.stopBus();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        @Override
        public void crashBus(Throwable t) {
            if (sent.compareAndSet(false, true)) {
                actionExecutor.execute(() -> {
                    try {
                        busControls.crashBus(t);
                    } catch (BusCrashException e) {
                        this.exception = e;
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        Optional<BusCrashException> waitOnEnded() throws InterruptedException {
            latch.await();
            return Optional.ofNullable(this.exception);
        }
    }

    class BusActionQueue implements BusActionExecutor, BusAction {

        private final BlockingQueue<BusAction> actionQueue = new LinkedBlockingQueue<>();

        @Override
        public void run() throws BusCrashException {

            BusAction action = null;
            try {
                action = actionQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (action != null) {
                action.run();
            }
        }

        @Override
        public void stopBus() {
            actionQueue.add(busControls::stopBus);
        }

        @Override
        public void crashBus(Throwable t) {
            actionQueue.add(() -> busControls.crashBus(t));
        }
    }
}
