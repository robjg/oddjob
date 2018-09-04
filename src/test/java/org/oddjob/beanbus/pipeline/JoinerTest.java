package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class JoinerTest {

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
    public void testStandalone() {

        WireTap<Integer> results = new WireTap<>();

        Joiner<Integer> test = new Joiner<>(results);

        FlushableConsumer<Integer> c1 = new IdentitySection<>(test.newJoinPont());
        FlushableConsumer<Integer> c2 = new IdentitySection<>(test.newJoinPont());
        FlushableConsumer<Integer> c3 = new IdentitySection<>(test.newJoinPont());

        c1.accept(1);
        c2.accept(2);
        c3.accept(3);

        List<Integer> resultLists;

        resultLists = results.toCollection().stream().collect(Collectors.toList());
        assertThat( resultLists.size(), is(0));

        c1.flush();

        resultLists = results.toCollection().stream().collect(Collectors.toList());
        assertThat( resultLists.size(), is(0));

        c2.flush();

        resultLists = results.toCollection().stream().collect(Collectors.toList());
        assertThat( resultLists.size(), is(0));

        c2.flush();

        resultLists = results.toCollection().stream().collect(Collectors.toList());
        assertThat( resultLists.size(), is(3));

        assertThat(resultLists.get(0), is(1));
        assertThat( resultLists.get(1), is(2));
        assertThat( resultLists.get(2), is(3));
    }

    @Test
    public void testJoiningInPipeline() {

        int sampleSize = 10_000;

        ExecutorService executor = Executors.newFixedThreadPool(3);

        AsyncPipeline<Integer> pipeline = new AsyncPipeline<Integer>(executor);

        WireTap<Integer> results = new WireTap<>();

        Joiner<Integer> test = new Joiner<>(results);

        List<FlushableConsumer<Integer>> joins = new ArrayList<>();

        for (int i = 0; i < 10; ++i) {
            joins.add(pipeline.createSection(
                    new Batcher<Integer>(
                            new Flattener<Integer>(test.newJoinPont()),
                            Integer.MAX_VALUE)));
        }

        FlushableConsumer<Integer> split = new FlushableConsumer<Integer>() {
            @Override
            public void accept(Integer data) {
                joins.get(data % 10).accept(data);
            }

            @Override
            public void flush() {
                joins.forEach(FlushableConsumer::flush);
            }
        };

        FlushableConsumer<Integer> start = pipeline.openWith(split);

        for (int i = 0; i < sampleSize; ++i) {
            start.accept(i);
        }

        start.flush();

        List<Integer> resultLists = results.toCollection()
                .stream()
                .collect(Collectors.toList());

        assertThat( resultLists.size(), is(sampleSize));

        List<Integer> firstFew = resultLists.stream().limit(5).collect(Collectors.toList());

        assertThat( firstFew, is(Arrays.asList(0, 10, 20, 30, 40)));

        executor.shutdown();
    }
}