package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JoinerTest {

    AtomicInteger flushCount = new AtomicInteger();

    class Tester<T> implements Section<T, T> {

        int flush;

        @Override
        public Pipe<T> linkTo(Consumer<? super T> next) {
            return new Pipe<T>() {
                @Override
                public void accept(T data) {
                    next.accept(data);
                }

                @Override
                public void flush() {
                    flush = flushCount.getAndIncrement();
                }
            };
        }
    }


    @Test
    public void testSimpleSplitJoin() {

        Pipeline<Integer> pipeline = SyncPipeline.start();

        Join<Integer, Integer> join = pipeline.join();

        join.join(pipeline.to(Mapper.identity()));
        join.join(pipeline.to(Mapper.identity()));

        Processor<Integer, List<Integer>> processor =
                join.to(Captures.toList())
                        .create();

        processor.accept(1);

        List<Integer> r = processor.complete();

        assertThat(r, is(Arrays.asList(1, 1)));
    }

    @Test
    public void testMultipleJoinSync() {

        testMultipleJoin(SyncPipeline.start(), SyncPipeline.withOptions());
    }

    @Test
    public void testMultipleJoinAsync() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        testMultipleJoin(AsyncPipeline2.start(executor), AsyncPipeline2.withOptions().async());

        executor.shutdown();
    }

    private void testMultipleJoin(Pipeline<Integer> pipeline, Pipeline.Options options) {


        Join<Integer, Integer> join1 = pipeline.join();
        Join<Integer, Integer> join2 = pipeline.join();
        Join<Integer, Integer> join3 = pipeline.join();

        join1.join(pipeline.to(Mapper.identity(), options));
        join1.join(pipeline.to(Mapper.identity()));

        join2.join(pipeline.to(Mapper.identity(), options));
        join2.join(pipeline.to(Mapper.identity()));

        join3.join(join1);
        join3.join(join2);

        Processor<Integer, List<Integer>> processor = join3.to(Captures.toList(), options)
                .create();

        processor.accept(1);

        List<Integer> results = processor.complete();

        assertThat(results, is( Arrays.asList(1,1,1,1)));
    }





    static class Reduce implements Section<Collection<? extends Number>, Integer> {

        @Override
        public Pipe<Collection<? extends Number>> linkTo(Consumer<? super Integer> next) {
            return new Pipe<Collection<? extends Number>>() {
                @Override
                public void accept(Collection<? extends Number> data) {
                    next.accept(data.stream()
                            .mapToInt(Number::intValue)
                            .max()
                            .orElse(0));
                }

                @Override
                public void flush() {

                }
            };
        }
    }

    @Test
    public void testJoiningInPipeline() {

        int sampleSize = 10_000;

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Pipeline<Integer> pipeline = SyncPipeline.start();

        Pipeline.Stage<Integer, Integer> from =
                pipeline.to(Splits.byIndex(data -> Collections.singleton(data % 10)));

        Join<Integer, Integer> join = pipeline.join();

        for (int i = 0; i < 10; ++i) {

            join.join(from.to(Folds.maxInt()));
        }

        Processor<Integer, List<Integer>> start =
                join.to(Captures.toList())
                        .create();

        for (int i = 0; i < sampleSize; ++i) {
            start.accept(i);
        }

        List<Integer> resultLists = start.complete();

        assertThat(resultLists.size(), is(10));

        Set<Integer> resultSet = resultLists.stream()
                .collect(Collectors.toSet());

        assertThat(resultSet, is(new HashSet<>(
                Arrays.asList(
                        9_999,
                        9_998,
                        9_997,
                        9_996,
                        9_995,
                        9_994,
                        9_993,
                        9_992,
                        9_991,
                        9_990
                ))));

        executor.shutdown();
    }

}