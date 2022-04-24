package org.oddjob.beanbus.destinations;

import org.oddjob.framework.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @param <E> The type of element on the queue.
 * @author rob
 * @oddjob.description A Queue for beans. A work in progress.
 * @oddjob.example A simple example.
 * <p>
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanQueueExample.xml}
 * @oddjob.example An example in BeanBus.
 * <p>
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanQueueExample2.xml}
 */
public class BusQueue<E> implements Consumer<E>, Iterable<E>, Service {

    private static final Logger logger = LoggerFactory.getLogger(BusQueue.class);

    private final static Object STOP = new Object();

    private volatile int capacity;

    private volatile BlockingQueue<Object> queue;

    private volatile String name;

    private final AtomicInteger taken = new AtomicInteger();

    private final  AtomicInteger waitingConsumers = new AtomicInteger();

    @Override
    public void start() {
        if (capacity == 0) {
            queue = new LinkedBlockingDeque<>();
        } else {
            queue = new ArrayBlockingQueue<>(capacity);
        }

        this.taken.set(0);
    }


    @Override
    public void stop() {
        logger.debug("Stopping Queue.");
        try {
            queue.put(STOP);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void accept(E bean) {
        try {
            queue.put(bean);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new BlockerIterator<>(queue, waitingConsumers, taken, toString() );
    }

    /**
     * The implementation of the blocking iterator.
     */
    static class BlockerIterator<E> implements Iterator<E> {

        private E next;

        private int taken;

        private final BlockingQueue<Object> queue;

        private final AtomicInteger waitingConsumers;

        private final AtomicInteger queueTaken;

        private final String name;

        BlockerIterator(BlockingQueue<Object> queue, AtomicInteger waitingConsumers,
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

    public int getSize() {
        Queue<?> queue = this.queue;
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
