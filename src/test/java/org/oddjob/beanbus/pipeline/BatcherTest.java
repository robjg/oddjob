package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class BatcherTest {

    @Test
    public void testStandalone() {


        List<List<Integer>> results = new ArrayList<>();

        Pipe<Integer> test = Batcher.<Integer>ofSize(2).linkTo(results::add);

        test.accept(1);
        test.accept(2);
        test.accept(3);
        test.accept(4);
        test.accept(5);

        test.flush();

        assertThat(results.size(), is(3));
        assertThat(results.get(0), is(Arrays.asList(1, 2)));
        assertThat(results.get(1), is(Arrays.asList(3, 4)));
        assertThat(results.get(2), is(Arrays.asList(5)));
    }

    @Test
    public void testExactBatchSizeStandalone() {

        List<List<Integer>> results = new ArrayList<>();

        Pipe<Integer> test = Batcher.<Integer>ofSize(2).linkTo(results::add);

        test.accept(1);
        test.accept(2);

        test.flush();

        assertThat(results.size(), is(1));
        assertThat(results.get(0), is(Arrays.asList(1, 2)));
    }

    static private class SomeSection implements Section<Collection<Number>, String> {

        @Override
        public Pipe<Collection<Number>> linkTo(Consumer<? super String> next) {
            return new Pipe<Collection<Number>>() {
                @Override
                public void accept(Collection<Number> data) {
                    next.accept(data.toString());
                }

                @Override
                public void flush() {
                }
            };
        }
    }

    @Test
    public void testBatchingToSuperType() {

        Pipeline<Integer> pipeline = SyncPipeline.begin();

        Processor<Integer, String> processor = pipeline.to(Batcher.<Number>ofSize(2))
            .to(new SomeSection())
                .to(Captures.single())
                .create();

        processor.accept(1);
        processor.accept(2);

        String result = processor.complete();

        assertThat(result, is("[1, 2]"));
    }


    @Test
    public void testBatchingInPipeline() {

        int sampleSize = 10_001;
        int batchSize = 100;
        int expectedBatches = sampleSize / batchSize + 1;

        assertThat(expectedBatches, is(101));

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Pipeline<Integer> pipeline = AsyncPipeline.begin(executor);

        Processor<Integer, List<List<Integer>>> start =
                pipeline.to(Batcher.ofSize(batchSize))
                        .to(Captures.toList())
                        .create();

        for (int i = 0; i < sampleSize; ++i) {
            start.accept(i);
        }

        List<List<Integer>> resultLists = start.complete();

        assertThat(resultLists.size(), is(expectedBatches));

        int fullBatches = 0;
        int partialBatches = 0;

        for (List<? extends Integer> batch : resultLists) {
            if (batch.size() == batchSize) {
                ++fullBatches;
            } else {
                ++partialBatches;
            }
        }
        assertThat(fullBatches, is(expectedBatches - 1));
        assertThat(partialBatches, is(1));

        executor.shutdown();
    }

}

