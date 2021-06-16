package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.BusFilter;
import org.oddjob.framework.adapt.HardReset;
import org.oddjob.framework.adapt.SoftReset;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @param <T> The type of bean being batched.
 * @author rob
 * @oddjob.description Provide batching of beans.
 * @oddjob.example Create Batches of 2 beans.
 * <p>
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BatcherExample.xml}
 */
public class Batcher<T> implements Consumer<T>, BusFilter<T, Collection<T>>, Flushable {

    private static final Logger logger = LoggerFactory.getLogger(Batcher.class);

    private String name;

    private int batchSize;

    private Consumer<? super Collection<T>> to;

    private volatile List<T> batch;

    private final AtomicInteger count = new AtomicInteger();

    @Start
    public void start() {
        batch = new ArrayList<>(batchSize);
    }

    @Stop
    public void stop() {
    }

    @HardReset
    @SoftReset
    public void reset() {
        count.set(0);
    }

    @Override
    public void accept(T bean) {

        batch.add(bean);
        count.incrementAndGet();

        if (batch.size() == batchSize) {
            dispatch();
        }
    }

    @Override
    public void flush() throws IOException {
        dispatch();
    }

    /**
     * Dispatch the beans. Called when a batch is ready of a trip
     * is ending.
     */
    protected void dispatch() {
        if (batch.isEmpty()) {
            return;
        }

        int batchSize = batch.size();

        if (to == null) {
            logger.info("Discarding batch of " + batchSize +
                    " beans because there is no destination.");
        } else {
            logger.info("Dispatching batch of " + batchSize +
                    " beans.");

            to.accept(batch);
        }

        batch = new ArrayList<>(batchSize);
    }

    public int getCount() {
        return count.get();
    }

    public int getSize() {
        Collection<?> batch = this.batch;
        if (batch == null) {
            return 0;
        } else {
            return batch.size();
        }
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public Consumer<? super Collection<T>> getTo() {
        return to;
    }

    @Override
    public void setTo(Consumer<? super Collection<T>> next) {
        this.to = next;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
