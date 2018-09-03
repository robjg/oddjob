package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AsyncPipelineTest {

    @Test
    public void whenStartJustConnectedToEndThenWorks() {

        AsyncPipeline test = new AsyncPipeline(Runnable::run);

        CollectionConsumer<String> results = new CollectionConsumer<>();

        FlushableConsumer<String> start = test.createSection(results);

        start.accept("Apple");

        assertThat(results.toCollection(), is( Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    private static class IdentitySection<T> implements FlushableConsumer<T> {

        private final FlushableConsumer<T> next;

        IdentitySection(FlushableConsumer<T> next) {
            this.next = next;
        }

        @Override
        public void accept(T data) {
            next.accept(data);
        }

        @Override
        public void flush() {
            next.flush();
        }

    }

    @Test
    public void registeredComponentWorks() {

        AsyncPipeline test = new AsyncPipeline(Runnable::run);

        CollectionConsumer<String> results = new CollectionConsumer<>();

        FlushableConsumer<String> section = test.createSection(new IdentitySection<>(results));

        FlushableConsumer<String> start = test.createSection(section);

        start.accept("Apple");

        assertThat(results.toCollection(), is( Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }


    @Test
    public void flushBlocks() {

        List<Runnable> work = new ArrayList<>();

        AsyncPipeline test = new AsyncPipeline(work::add, 1);

        CollectionConsumer<String> results = new CollectionConsumer<>();

        FlushableConsumer<String> section = test.createSection(new IdentitySection<>(results));

        FlushableConsumer<String> start = test.openWith(section);

        start.accept("Apple");

        try {
            start.flush();
            fail("Should timeout");
        }
        catch (IllegalStateException e) {
            // expected.
        }

        assertThat(work.size(), is( 1 ));

        work.forEach( Runnable::run );


        assertThat(results.toCollection(), is( Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    @Test
    public void workBlocks() {

        List<Runnable> work = new ArrayList<>();

        AsyncPipeline test = new AsyncPipeline(work::add, 1);

        CollectionConsumer<String> results = new CollectionConsumer<>();

        FlushableConsumer<String> section = test.createBlockSection(results, 0);

        FlushableConsumer<String> start = test.openWith(section);

        start.accept("Apple");

        try {
            start.accept( "Pear");
            fail("Should timeout");
        }
        catch (IllegalStateException e) {
            // expected.
        }

        assertThat(work.size(), is( 1 ));

        work.forEach( Runnable::run );

        start.accept( "Pear");

        work.forEach( Runnable::run );

        assertThat(results.toCollection(), is( Arrays.asList("Apple", "Pear").stream().collect(Collectors.toList())));

    }
}