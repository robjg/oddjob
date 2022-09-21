package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.BusFilter;
import org.oddjob.framework.adapt.HardReset;
import org.oddjob.framework.adapt.SoftReset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @oddjob.description Takes an Iterable and Unpacks it.
 *
 * @author Rob
 *
 * @oddjob.example Un Batching 2 Lists.
 * <p>
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/UnPackExample.xml}
 *
 * @param <T> The type of bean being batched.
 */
public class UnPack<T> implements BusFilter<Iterable<T>, T> {

    private static final Logger logger = LoggerFactory.getLogger(UnPack.class);

    private String name;

    private Consumer<? super T> to;

    private final AtomicInteger count = new AtomicInteger();


    @HardReset
    @SoftReset
    public void reset() {
        count.set(0);
    }

    @Override
    public void accept(Iterable<T> beans) {

        for (T t : beans) {
            count.incrementAndGet();

            if (to != null) {

                to.accept(t);
            }
        }
    }


    public int getCount() {
        return count.get();
    }

    public Consumer<? super T> getTo() {
        return to;
    }

    @Override
    public void setTo(Consumer<? super T> next) {
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
