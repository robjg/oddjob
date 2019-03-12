package org.oddjob.events.example;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.events.EventOf;
import org.oddjob.events.EventSourceBase;
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
public class FactSubscriber<T> extends EventSourceBase<T> {

    private static final Logger logger = LoggerFactory.getLogger(FactSubscriber.class);

    private volatile String name;

    private volatile String query;

    private volatile FactStore factStore;

    private volatile EventOf<T> last;

    @Override
    public Restore doStart(Consumer<? super EventOf<T>> consumer) throws Exception {

        FactStore factStore = Optional.ofNullable(this.factStore)
                .orElseThrow(() -> new IllegalArgumentException("No Fact Store"));

        String query = Optional.ofNullable(this.query)
                .orElseThrow(() -> new IllegalArgumentException("No Query"));

        class FactStoreConsumer implements Consumer<EventOf<T>> {

            @Override
            public void accept(EventOf<T> t) {
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

    public EventOf<T> getLast() {
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
