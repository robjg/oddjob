package org.oddjob.beanbus.destinations;

import org.oddjob.framework.Service;
import org.oddjob.framework.adapt.HardReset;
import org.oddjob.framework.adapt.SoftReset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @oddjob.description A Queue for beans. A work in progress.
 *
 * @oddjob.example A simple example.
 * <p>
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanQueueExample.xml}
 *
 * @oddjob.example An example in BeanBus.
 * <p>
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanQueueExample2.xml}
 *
 * @author rob
 *
 * @param <E> The type of element on the queue.
 */
public class BusQueue<E> implements Consumer<E>, Iterable<E>, Service {

    private static final Logger logger = LoggerFactory.getLogger(BusQueue.class);

    private final static Object STOP = new Object();

    /**
     * @oddjob.property
     * @oddjob.description Capacity of the queue before it blocks.
     * @oddjob.required No, defaults to no limit.
     */
    private volatile int capacity;

    private volatile CompletableFuture<BlockingQueue<Object>> queueFuture = new CompletableFuture<>();

    /**
     * @oddjob.property
     * @oddjob.description The name of this component.
     * @oddjob.required No.
     */
    private volatile String name;

    /**
     * @oddjob.property
     * @oddjob.description The number of items taken from the queue.
     * @oddjob.required Read only.
     */
    private final AtomicInteger taken = new AtomicInteger();

    /**
     * @oddjob.property
     * @oddjob.description The number of consumers waiting.
     * @oddjob.required Read only.
     */
    private final  AtomicInteger waitingConsumers = new AtomicInteger();

    @Override
    public void start() {
        if (capacity == 0) {
            queueFuture.complete(new LinkedBlockingDeque<>());
        } else {
            queueFuture.complete(new ArrayBlockingQueue<>(capacity));
        }
    }


    @Override
    public void stop() {
        logger.debug("Stopping Queue.");
        try {
            queueFuture.get().put(STOP);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(E bean) {
        try {
            queueFuture.getNow(null).put(bean);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new BlockerIterator<>(queueFuture, waitingConsumers, taken, toString() );
    }

    @HardReset
    @SoftReset
    public void onReset() {
        this.queueFuture = new CompletableFuture<>();
        this.taken.set(0);
    }

    /**
     * The implementation of the blocking iterator.
     */
    static class BlockerIterator<E> implements Iterator<E> {

        private E next;

        private int taken;

        private final Future<BlockingQueue<Object>> queue;

        private final AtomicInteger waitingConsumers;

        private final AtomicInteger queueTaken;

        private final String name;

        BlockerIterator(Future<BlockingQueue<Object>> queue, AtomicInteger waitingConsumers,
                AtomicInteger queueTaken, String name) {
            this.queue = Objects.requireNonNull(queue, "Queue Not Started");
            this.waitingConsumers = waitingConsumers;
            this.queueTaken = queueTaken;
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean hasNext() {

            if (next != null) {
                return true;
            }

            BlockingQueue<Object> queue = null;
            try {
                queue = this.queue.get();
            } catch (InterruptedException e) {
                logger.info("Interrupted waiting for queue.");
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            Object first = queue.poll();

            if (first == null) {

                // queue must be empty.
                try {
                    waitingConsumers.incrementAndGet();

                    first = queue.take();
                } catch (InterruptedException e) {
                    logger.info("Interrupted waiting for next value.");
                    Thread.currentThread().interrupt();
                    return false;
                } finally {
                    waitingConsumers.decrementAndGet();
                }
            }

            if (first == STOP) {
                try {
                    queue.put(STOP);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return false;
            } else {
                next = (E) first;

                ++this.taken;
                queueTaken.incrementAndGet();

                return true;
            }
        }

        @Override
        public E next() {
            try {
                return next;
            } finally {
                next = null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "Iterator for " + name +
                    ", taken=" + taken;
        }
    }


    /**
     * @oddjob.property
     * @oddjob.description The size of the queue.
     * @oddjob.required Read only.
     */
    public int getSize() {
        Queue<?> queue = this.queueFuture.getNow(null);
        return (queue == null ? 0 : queue.size());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTaken() {
        return taken.get();
    }

    /**
     * Allows an item to be place on the queue with set property.
     *
     * @param item An item.
     */
    public void setPut(E item) {
        accept(item);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getWaitingConsumers() {
        return waitingConsumers.get();
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
