package org.oddjob.events.example;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.events.InstantEvent;
import org.oddjob.events.InstantEventSourceBase;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Subscribes to an {@link FactStore}.
 *
 * @param <T>
 */
public class FactSubscriber<T> extends InstantEventSourceBase<T> {

    private static final Logger logger = LoggerFactory.getLogger(FactSubscriber.class);

    private volatile String name;

    private volatile String query;

    private volatile FactStore factStore;

    private volatile InstantEvent<T> last;

    @Override
    public Restore doStart(Consumer<? super InstantEvent<T>> consumer) throws Exception {

        FactStore factStore = Optional.ofNullable(this.factStore)
                .orElseThrow(() -> new IllegalArgumentException("No Fact Store"));

        String query = Optional.ofNullable(this.query)
                .orElseThrow(() -> new IllegalArgumentException("No Query"));

        class FactStoreConsumer implements Consumer<InstantEvent<T>> {

            @Override
            public void accept(InstantEvent<T> t) {
                last = t;
                logger.info("Received: {}", t);
                consumer.accept(t);
            }

            @Override
            public String toString() {
                return "Consumer for " + FactSubscriber.this.toString();
            }
        }

        return factStore.subscribe(query, new FactStoreConsumer());
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public InstantEvent<T> getLast() {
        return last;
    }

    @ArooaAttribute
    @Inject
    public void setFactStore(FactStore factStore) {
        this.factStore = factStore;
    }

    public FactStore getFactStore() {
        return factStore;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(name).orElseGet(
                () -> getClass().getSimpleName());
    }

}
