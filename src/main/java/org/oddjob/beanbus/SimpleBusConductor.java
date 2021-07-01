package org.oddjob.beanbus;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ExceptionListener;
import java.io.Flushable;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A Simple Bus Conductor that manages the lifecycle of components.
 */
public class SimpleBusConductor implements Runnable, BusConductor {

    private static final Logger logger = LoggerFactory.getLogger(SimpleBusConductor.class);

    private final LinkedList<Object> components;

    private final Deque<Object> started = new ConcurrentLinkedDeque<>();

    private volatile boolean stop;

    public SimpleBusConductor(Object... components) {
        this.components = new LinkedList<>();
        for (int i = 0; i < components.length; ++i) {
            Object component = Objects.requireNonNull(components[i], "Component [" + i + "] is null");
            this.components.add(component);
        }
    }

    @Override
    public void close() {

        stop = true;
        flush();
        closeNoFlush();
    }

    protected void closeNoFlush() {

        while (!started.isEmpty()) {
            Object component = started.remove();
            if (component instanceof Stoppable) {
                try {
                    logger.debug("Stopping [{}]", component);
                    ((Stoppable) component).stop();
                } catch (FailedToStopException e) {
                    logger.warn("failed stopping [" + component + "]", e);
                }
            }
            else if (component instanceof AutoCloseable) {
                try {
                    logger.debug("Closing [{}]", component);
                    ((AutoCloseable) component).close();
                } catch (Exception e) {
                    logger.warn("failed closing [" + component + "]", e);
                }
            }
        }
    }

    @Override
    public void flush() {

        for (Object component : started) {
            if (component instanceof Flushable) {
                try {
                    logger.debug("Flushing [{}]", component);
                    ((Flushable) component).flush();
                } catch (Exception e) {
                    logger.warn("failed flushing [" + component + "]", e);
                    actOnBusCrash(e);
                    return;
                }
            }
        }
    }

    public void actOnBusCrash(Throwable e) {

        stop = true;

        for (Object component : started) {
            if (component instanceof ExceptionListener) {
                logger.debug("Notifying [{}] of Exception {}", component, e);
                if (e instanceof Exception) {
                    ((ExceptionListener) component).exceptionThrown((Exception) e);
                }
                else {
                    ((ExceptionListener) component).exceptionThrown(new Exception(e));
                }
            }
        }

        closeNoFlush();
    }


    @Override
    public void run() {

        stop = false;

        if (!started.isEmpty()) {
            throw new IllegalStateException("Started not empty");
        }

        Iterable<Object> reverseIterable = components::descendingIterator;

        for (Object component : reverseIterable) {

            if (stop) {
                break;
            }

            // add before running in case stopped
            this.started.addFirst(component);

            if (component instanceof Runnable) {
                try {
                    logger.debug("Running [{}]", component);
                    ((Runnable) component).run();
                }
                catch (RuntimeException e) {
                    actOnBusCrash(e);
                    throw e;
                }
            }
        }
    }
}
