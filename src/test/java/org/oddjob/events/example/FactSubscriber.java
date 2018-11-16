package org.oddjob.events.example;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.events.EventSourceBase;
import org.oddjob.util.Restore;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

public class FactSubscriber<T> extends EventSourceBase<T> {

    private volatile String name;

    private volatile String query;

    private volatile FactStore factStore;

    private volatile T last;

    @Override
    public Restore doStart(Consumer<? super T> consumer) throws Exception {

        FactStore factStore = Optional.ofNullable(this.factStore)
                .orElseThrow(() -> new IllegalArgumentException("No Fact Store"));
        String query = Optional.ofNullable(this.query)
                .orElseThrow(() -> new IllegalArgumentException("No Query"));

        return factStore.<T>subscribe(query, t -> {
            last = t;
            consumer.accept(t);
        });
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public T getLast() {
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
