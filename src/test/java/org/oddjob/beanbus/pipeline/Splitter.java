package org.oddjob.beanbus.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class Splitter<T> implements Pipe<T> {

    private final List<Pipe<? super T>> splits;

    private final Function< T, Collection<Pipe<? super T>>> mapping;

    Executor executor;

    public Splitter(List<Pipe<? super T>> splits, Function<T, Collection<Pipe<? super T>>> mapping) {
        this.splits = splits;
        this.mapping = mapping;
    }

    @Override
    public void accept(T data) {

        Collection<Pipe<? super T>> sendTo = mapping.apply(data);
        sendTo.forEach(pipe -> pipe.accept(data));
    }

    @Override
    public void flush() {
        List<CompletableFuture<?>> cfs = new ArrayList<>(splits.size());
        splits.forEach(p -> cfs.add(CompletableFuture.runAsync(() -> p.flush(), executor)));
        CompletableFuture<?> all = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()]));
        try {
            all.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
