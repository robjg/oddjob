package org.oddjob.beanbus.drivers;

import org.oddjob.Stoppable;
import org.oddjob.beanbus.Outbound;
import org.oddjob.framework.adapt.HardReset;
import org.oddjob.framework.adapt.SoftReset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @oddjob.description Drives data from  an iterable (such as a {@link org.oddjob.arooa.types.ListType})
 * through a Bean Bus. It can also be used outside Bean Bus to push data to any {@link Consumer}.
 *
 * @oddjob.example Drive 3 Beans through a Bean Bus.
 *
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanTransformerExample.xml}
 *
 * @param <T> The generic type of the Iterable.
 *
 * @author Rob
 */
public class IterableBusDriver<T>
        implements Runnable, Stoppable, Outbound<T> {

    private static final Logger logger = LoggerFactory.getLogger(IterableBusDriver.class);

    private Iterable<? extends T> values;

    private Consumer<? super T> to;

    private volatile boolean stop;

    private String name;

    private final AtomicInteger count = new AtomicInteger();

    private final AtomicReference<Thread> executionThread = new AtomicReference<>();

    @HardReset
    @SoftReset
    public void reset() {
        count.set(0);
    }

    @Override
    public void run() {

        Iterable<? extends T> beans = Objects.requireNonNull(this.values, "No beans.");
        Consumer<? super T> to = Objects.requireNonNull(this.to, "No to.");

        stop = false;

        Iterator<? extends T> current = beans.iterator();

        executionThread.set(Thread.currentThread());
        try {
            while (!stop) {
                if (!current.hasNext()) {
                    break;
                }

                to.accept(current.next());

                count.incrementAndGet();
            }
        } finally {
            executionThread.set(null);
            // Clear the interrupt flag just in case it's set by stop.
            if (Thread.interrupted()) {
                logger.debug("Thread interrupted.");
            }
        }

        logger.info("Accepted " + count + " beans.");
    }

    @Override
    public void stop() {
        this.stop = true;
        Optional.ofNullable(executionThread.get()).ifPresent(t -> {
            logger.debug("Interrupting execution thread.");
            t.interrupt();
        });
    }

    public Iterable<? extends T> getValues() {
        return values;
    }

    /**
     * The data to iterate over.
     *
     * @param iterable The iterable.
     */
    public void setValues(Iterable<? extends T> iterable) {
        this.values = iterable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the destination.
     *
     * @param to
     */
    @Override
    public void setTo(Consumer<? super T> to) {
        this.to = to;
    }

    public Consumer<? super T> getTo() {
        return to;
    }

    public int getCount() {
        return count.get();
    }

    @Override
    public String toString() {
        if (name == null) {
            return getClass().getSimpleName();
        } else {
            return name;
        }
    }
}
