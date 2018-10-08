package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SplitsTest {

    @Test
    public void testSimpleSplitSync() {

        testSimpleSplit(SyncPipeline.start());

    }

    @Test
    public void testSimpleSplitAsync() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        testSimpleSplit(AsyncPipeline2.start(executor));

        executor.shutdown();
    }

    public void testSimpleSplit(Pipeline<Integer> pipeline ) {

        Link<Integer, Integer> split =
                pipeline.to(Splits.byIndex(i -> Collections.singleton(i % 2)));

        Pipeline.Join<Integer, Integer> join = pipeline.join();

        join.join(split.to(Mapper.identity()));
        join.join(split.to(Mapper.identity()));

        Processor<Integer, List<Integer>> processor =
                join.to(Captures.toList())
                        .create();

        processor.accept(1);

        List<Integer> r = processor.complete();

        assertThat(r, is(Arrays.asList(1)));
    }
}
