package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RealisticPipelineTest {

    @Test
    public void testReallySimple() {

        Pipeline<Integer> pipeline = SyncPipeline.start();

        Processor<Integer, List<Integer>> processor =
                pipeline.to(Captures.toList())
                        .create();

        processor.accept(1);
        processor.accept(2);
        processor.accept(3);

        List<Integer> results = processor.complete();

        assertThat( results, is(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void testSimple() {

        Pipeline<Integer> pipeline = SyncPipeline.start();

        Processor<Integer, String> processor = pipeline
                .to(new Mapper<>(i -> i + 1))
                .to(Folds.with(0, (i, r) -> r + i))
                .to(new Mapper<>(i -> i.toString()))
                .to(Captures.single())
                .create();

        processor.accept(1);
        processor.accept(2);
        processor.accept(3);

        String result = processor.complete();

        assertThat(result, is("9"));
    }

    static class Count implements Section<Collection<? extends Object>, Long> {

        @Override
        public Pipe<Collection<? extends Object>> linkTo(Consumer<? super Long> next) {

            return new Pipe<Collection<? extends Object>>() {

                @Override
                public void accept(Collection<? extends Object> data) {
                    if (data.size() > 0) {
                        next.accept(data.stream()
                                .mapToInt(ignored -> 1)
                                .count());
                    }
                }

                @Override
                public void flush() {

                }
            };
        }
    }

    @Test
    public void testSomethingRealistic() {

        int sampleSize = 100_000;
        int batchSize = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Pipeline<Integer> pipeline = AsyncPipeline2.start(executor);

        Pipeline.Stage<Integer, Integer> splits =
                pipeline.to(Splits.byIndex(i -> Collections.singleton(i % 10)));

        Join<Integer, Long> join = pipeline.join();

        for (int i = 0; i < 10; ++i) {

            join.join(
                    splits.to(Batcher.ofSize(1000))
                            .to(new Count(),
                                    AsyncPipeline2.withOptions().async()));
        }

        Processor<Integer, List<Long>> start =
                join.to(Captures.toList()).create();

        for (int i = 0; i < sampleSize; ++i) {
            start.accept(i);
        }

        List<Long> resultLists = start.complete();

        assertThat(resultLists.size(), is(sampleSize / batchSize));

        executor.shutdown();
    }

}
