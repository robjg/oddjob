package org.oddjob.events;

import org.oddjob.FailedToStopException;
import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.beanbus.Outbound;
import org.oddjob.events.state.EventState;
import org.oddjob.events.state.EventStateChanger;
import org.oddjob.events.state.EventStateHandler;
import org.oddjob.framework.extend.BasePrimary;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.*;
import org.oddjob.util.Restore;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * Base class for components that want Event Icons because the objects the emmit are in the style of events
 * rather than streams of data.
 *
 * @param <T> The type of the event.
 */
abstract public class EventServiceBase<T> extends BasePrimary
        implements Outbound<T>, Runnable, Resettable, Stateful, Stoppable {

    /** Handle state. */
    private transient volatile EventStateHandler stateHandler;

    /** Used to notify clients of an icon change. */
    private transient volatile IconHelper iconHelper;

    /** Changes state */
    private transient volatile EventStateChanger stateChanger;

    /**
     * @oddjob.property
     * @oddjob.description The destination events will be sent to.
     * @oddjob.required Maybe. Set automatically by some parent components.
     */
    private Consumer<? super T> to;

    /**
     * provided by subclasses to clean up on stop.
     */
    private Restore restore;

    /**
     * Constructor.
     */
    public EventServiceBase() {
        completeConstruction();
    }

    private void completeConstruction() {
        stateHandler = new EventStateHandler(this);
        iconHelper = new IconHelper(this,
                StateIcons.iconFor(stateHandler.getState()));
        stateChanger = new EventStateChanger(stateHandler, iconHelper, this::save);
    }

    @Override
    protected EventStateHandler stateHandler() {
        return stateHandler;
    }

    @Override
    protected IconHelper iconHelper() {
        return iconHelper;
    }

    protected final StateChanger<EventState> getStateChanger() {
        return stateChanger;
    }

    @Override
    public void run() {

        Consumer<? super T> consumer = Objects.requireNonNull(this.to);

        if (!stateHandler().waitToWhen(new IsExecutable(),
                () -> getStateChanger().setState(EventState.CONNECTING))) {
            throw new IllegalStateException("Not Stopped!");
        }

        final Semaphore barrier = new Semaphore(1);
        Consumer<T> consumerWrapper = value ->  {
            try (Restore ignored = ComponentBoundary.push(loggerName(), EventServiceBase.this)) {
                barrier.acquire();
                stateHandler().waitToWhen(s -> true,
                        () -> getStateChanger().setState(EventState.FIRING));
                logger().debug("Received event {}", value);
                consumer.accept(value);
                stateHandler().waitToWhen(new IsStoppable(),
                        () -> getStateChanger().setState(EventState.TRIGGERED));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            finally {
                barrier.release();
            }
        };

        try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {

            logger().info("Starting");

            try {
                configure();

                Restore restore = doStart(consumerWrapper);

                stateHandler().waitToWhen(s -> s == EventState.CONNECTING,
                        () -> getStateChanger().setState(EventState.WAITING));

                this.restore = () -> {
                    try (Restore ignored2 = ComponentBoundary.push(loggerName(), EventServiceBase.this)) {
                        restore.close();
                        logger().info("Stopped");
                    }
                    catch (RuntimeException e) {
                        stateHandler().waitToWhen(s -> true,
                                () -> getStateChanger().setStateException(e));
                        throw e;
                    }
                    stateHandler().waitToWhen(new IsStoppable(),
                            () -> {
                                State state = stateHandler().lastStateEvent().getState();
                                if (state == EventState.TRIGGERED || state == EventState.FIRING) {
                                    getStateChanger().setState(EventState.COMPLETE);
                                }
                                else {
                                    getStateChanger().setState(EventState.INCOMPLETE);
                                }
                            });
                };

            }
            catch (Exception e) {
                logger().error("Exception starting:", e);
                stateHandler().runLocked(() -> getStateChanger().setStateException(e));
            }
        }
    }

    protected void setStateException(Throwable e) {
        stateHandler().runLocked(() -> getStateChanger().setStateException(e));
    }

    protected abstract Restore doStart(Consumer<? super T> consumer) throws Exception;


    /**
     * Allow subclasses to indicate they are
     * stopping. The subclass must still implement
     * Stoppable.
     *
     * @throws FailedToStopException
     */
    public final void stop() throws FailedToStopException {
        stateHandler.assertAlive();

        Optional.ofNullable(this.restore).ifPresent(Restore::close);
    }

    @Override
    public boolean softReset() {
        try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {
            return stateHandler.waitToWhen(new IsSoftResetable(), () -> {
                onSoftReset();
                getStateChanger().setState(EventState.READY);
                logger().info("Soft Reset complete.");
            });
        }
    }

    @Override
    public boolean hardReset() {
        try (Restore ignored = ComponentBoundary.push(loggerName(), this)) {
            return stateHandler.waitToWhen(new IsHardResetable(), () -> {
                onHardReset();
                getStateChanger().setState(EventState.READY);
                logger().info("Hard Reset complete.");
            });
        }
    }

    /**
     * Allow subclasses to do something on a soft reset. Defaults to {@link #onReset()}
     */
    protected void onSoftReset() {
        onReset();
    }

    /**
     * Allow sub classes to do something on a hard reset. Defaults to {@link #onReset()}
     */
    protected void onHardReset() {
        onReset();
    }

    /**
     * Allow sub classes to do something on reset.
     */
    protected void onReset() {

    }


    @Override
    public void setTo(Consumer<? super T> destination) {
        this.to = destination;
    }

    /**
     * Internal method to fire state.
     */
    protected void fireDestroyedState() {

        if (!stateHandler().waitToWhen(new IsAnyState(), () -> {
            stateHandler().setState(EventState.DESTROYED);
            stateHandler().fireEvent();
        })) {
            throw new IllegalStateException("[" + this + "] Failed set state DESTROYED");
        }
        logger().debug("[" + this + "] Destroyed.");
    }
}
