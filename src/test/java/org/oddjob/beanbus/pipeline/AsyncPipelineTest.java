package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AsyncPipelineTest {

    @Test
    public void whenStartJustConnectedToEndThenWorks() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Pipeline<String> test = AsyncPipeline.begin(executor);

        Processor<String, List<String>> start =
                test.to(Captures.toList(),
                        AsyncPipeline.options().async())
                        .create();

        start.accept("Apple");

        List<String> results = start.complete();

        assertThat(results, is(Arrays.asList("Apple")));

        executor.shutdown();
    }

    private static class IdentitySection<T> implements Section<T, T> {

        @Override
        public Pipe<T> linkTo(Consumer<? super T> next) {
            return new Pipe<T>() {
                @Override
                public void accept(T data) {
                    next.accept(data);
                }

                @Override
                public void flush() {

                }
            };
        }
    }

    @Test
    public void registeredComponentWorks() {

        Pipeline<String> test = AsyncPipeline.begin(Runnable::run);

        Pipeline.Stage<String, String> begin = test
                .to(new IdentitySection<>());

        Join<String, String> join = test.join();
        join.join(begin);

        Processor<String, List<String>> processor =
                join.to(Captures.toList())
                        .create();

        processor.accept("Apple");

        List<String> results = processor.complete();

        assertThat(results, is(Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    @Test
    public void completeBlocks() {

        List<Runnable> work = Collections.synchronizedList(new ArrayList<>());

        Pipeline<String> test = AsyncPipeline.begin(work::add);

        Processor<String, List<String>> start =
                test.to(Captures.toList(), AsyncPipeline.options().async())
                        .create();

        start.accept("Apple");

        AtomicBoolean isBlocked = new AtomicBoolean(true);

        new Thread(() -> {
            // Testing by chance...
            Thread.yield();
            assertThat(work.size(), is( 1));
            isBlocked.set(false);
            work.get(0).run();
            while (work.size() < 2) {
                Thread.yield();
            }
            work.get(1).run();
        }).start();

        List<String> results = start.complete();
        assertThat(isBlocked.get(), is(false));

        assertThat(results, is(Arrays.asList("Apple")));
    }

    @Test
    public void workBlocks() {

        List<Runnable> work = Collections.synchronizedList(new ArrayList<>());

        AsyncPipeline<String> test = AsyncPipeline.begin(work::add);

        Processor<String, List<String>> processor =
                test.to(Captures.toList(), AsyncPipeline.options().async().maxWork(1))
                        .create();

        processor.accept("Apple");

        new Thread(() -> {
            while(!test.isBlocked()) {
                Thread.yield();
            }
            work.get(0).run();
        }).start();

        processor.accept("Pear");

        assertThat(work.size(), is(2));

        work.get(1).run();

        new Thread(() -> {
            while(work.size() < 3) {
                Thread.yield();
            }
            work.get(2).run();
        }).start();

        List<String> results = processor.complete();

        assertThat(results, is(Arrays.asList("Apple", "Pear")));
    }

    @Test
    public void canCreateSection() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        AsyncPipeline<String> test = AsyncPipeline.begin(executor);

        Section<String, Set<String>> section =
                test.to(Pipes.map(s -> "Hello " + s))
                        .to(Captures.toSet(), AsyncPipeline.options().async())
                        .asSection();

        Pipeline<String> outer = new SyncPipeline<>();

        Processor<String, Set<String>> processor = outer.to(section).create();

        processor.accept("Rod");
        processor.accept("Jane");
        processor.accept("Freddy");

        Set<String> results = processor.complete();

        executor.shutdown();

        assertThat(results.size(), is(3));

        assertThat(results.contains("Hello Rod"), is(true));
        assertThat(results.contains("Hello Jane"), is(true));
        assertThat(results.contains("Hello Freddy"), is(true));

    }

    @Test
    public void testAllThreadUsed() {

        CountDownLatch latch = new CountDownLatch(3);
        Set<Thread> threads = ConcurrentHashMap.newKeySet();
        Section<String, ? super String> blockingWork =
                n -> data -> {
                threads.add(Thread.currentThread());
                latch.countDown();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    n.accept(data);
            };

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Processor<String, Long> processor = AsyncPipeline.<String>begin(executor)
                .to(blockingWork, AsyncPipeline.options().async())
                .to(Folds.count())
                .create();

        processor.accept("a");
        processor.accept("b");
        processor.accept("c");

        Long result =  processor.complete();

        assertThat(result, is(3L));

        assertThat(threads.size(), is(3));
    }

    @Test
    public void doALotOfWork() {

        AtomicReference<Consumer<Integer>> start = new AtomicReference<>();

        Section<Integer, Object> section = next -> data -> {
            if (data > 0) {
                for (int i = 0; i < 10; ++i) {
                    start.get().accept(data - 1);
                }
            }
            else {
                next.accept(Boolean.TRUE);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Processor<Integer, Long> processor = AsyncPipeline.<Integer>begin(executor)
                .to(section, AsyncPipeline.options().async())
                .to(Folds.count())
                .create();

        start.set(processor);

        processor.accept(5);

        Long result = processor.complete();

        assertThat(result, is(100_000L));
    }

}