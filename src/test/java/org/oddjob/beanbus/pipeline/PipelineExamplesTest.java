package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PipelineExamplesTest {

    @Test
    public void testReallySimple() {

        Pipeline<Integer> pipeline = SyncPipeline.begin();

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
    public void testMappingsAndFolds() {

        Pipeline<Integer> pipeline = SyncPipeline.begin();

        Processor<Integer, String> processor = pipeline
                .to(Pipes.map((i -> i + 1)))
                .to(Pipes.fold(0, (Integer::sum)))
                .to(Pipes.map(Object::toString))
                .create();

        processor.accept(1);
        processor.accept(2);
        processor.accept(3);

        String result = processor.complete();

        assertThat(result, is("9"));
    }

    static class Count implements Section<Collection<?>, Long> {

        @Override
        public Pipe<Collection<?>> linkTo(Consumer<? super Long> next) {

            return new Pipe<Collection<?>>() {

                @Override
                public void accept(Collection<?> data) {
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
    public void testHighVolumeAsyncSplitsAndJoins() {

        int sampleSize = 100_000;
        int batchSize = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Pipeline<Integer> pipeline = AsyncPipeline.begin(executor);

        Connector<Integer, Integer> splits =
                pipeline.to(Splits.roundRobin());

        Join<Integer, Long> join = pipeline.join();

        for (int i = 0; i < 10; ++i) {

            join.join(
                    splits.to(Pipes.batcher(batchSize))
                            .to(new Count(),
                                    AsyncPipeline.options().async()));
        }

        Processor<Integer, List<Long>> start =
                join.to(Captures.toList()).create();

        for (int i = 0; i < sampleSize; ++i) {
            start.accept(i);
        }

        List<Long> resultLists = start.complete();

        assertThat(resultLists.size(), is(sampleSize / batchSize));
        resultLists.forEach(c -> assertThat(c, is((long) batchSize)));
        executor.shutdown();
    }

}
