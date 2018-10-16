package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JoinerTest {

    private AtomicInteger flushCount = new AtomicInteger();

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

        Pipeline<Integer> pipeline = SyncPipeline.begin();

        Join<Integer, Integer> join = pipeline.join();

        join.join(pipeline.to(Pipes.identity()));
        join.join(pipeline.to(Pipes.identity()));

        Processor<Integer, List<Integer>> processor =
                join.to(Captures.toList())
                        .create();

        processor.accept(1);

        List<Integer> r = processor.complete();

        assertThat(r, is(Arrays.asList(1, 1)));
    }

    @Test
    public void testMultipleJoinSync() {

        testMultipleJoin(SyncPipeline.begin(), SyncPipeline.options());
    }

    @Test
    public void testMultipleJoinAsync() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        testMultipleJoin(AsyncPipeline.begin(executor), AsyncPipeline.options().async());

        executor.shutdown();
    }

    private void testMultipleJoin(Pipeline<Integer> pipeline, Pipeline.Options options) {


        Join<Integer, Integer> join1 = pipeline.join();
        Join<Integer, Integer> join2 = pipeline.join();
        Join<Integer, Integer> join3 = pipeline.join();

        join1.join(pipeline.to(Pipes.identity(), options));
        join1.join(pipeline.to(Pipes.identity()));

        join2.join(pipeline.to(Pipes.identity(), options));
        join2.join(pipeline.to(Pipes.identity()));

        join3.join(join1);
        join3.join(join2);

        Processor<Integer, List<Integer>> processor = join3.to(Captures.toList(), options)
                .create();

        processor.accept(1);

        List<Integer> results = processor.complete();

        assertThat(results, is( Arrays.asList(1,1,1,1)));
    }

    @Test
    public void testJoinSeparatePathsSync() {

        testJoinSeparatePath(SyncPipeline.begin());
    }

    @Test
    public void testJoinSeparatePathsAsync() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        testJoinSeparatePath(AsyncPipeline.begin(executor));

        executor.shutdown();
    }

    private void testJoinSeparatePath(Pipeline<Integer> pipeline) {

        Join<Integer, Integer> join1 = pipeline.join();
        Join<Integer, Integer> join2 = pipeline.join();
        Join<Integer, Integer> join3 = pipeline.join();
        Join<Integer, Integer> join4 = pipeline.join();

        Connector<Integer, Integer> start = pipeline.to(Pipes.identity());

        join1.join(start.to(Pipes.identity()));
        join1.join(start.to(Pipes.identity()));

        join2.join(start.to(Pipes.identity()));
        join2.join(start.to(Pipes.identity()));

        join3.join(join1);
        join3.join(join2);

        join4.join(join3);
        join4.join(join1.to(Pipes.test(data -> false)));
        join4.join(join2.to(Pipes.test(data -> false)));

        Processor<Integer, Integer> processor =
                join4.to(Pipes.fold(0, (a, t) -> a += t ))
                .create();

        processor.accept(1);

        Integer result = processor.complete();

        assertThat(result, is(4));
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

        Pipeline<Integer> pipeline = AsyncPipeline.begin(executor);

        Pipeline.Stage<Integer, Integer> from =
                pipeline.to(Splits.roundRobin(),
                        AsyncPipeline.options().async());

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

        Set<Integer> resultSet = new HashSet<>(resultLists);

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