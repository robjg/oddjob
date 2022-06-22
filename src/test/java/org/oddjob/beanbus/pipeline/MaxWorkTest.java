package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MaxWorkTest {

    @Test
    public void testFlaggedWhenWorkExceeded() {

        List<String> results = new ArrayList<>();

        MaxWork maxWork = new MaxWork(Runnable::run,
                1,
                () -> results.add("block"),
                () -> results.add("un block"));

        maxWork.execute(() -> results.add("execute"));

        assertThat(results, is( Arrays.asList("block", "execute", "un block")));

    }

    @Test
    public void testNotFlaggedWhenWorkNotExceeded() {

        List<String> results = new ArrayList<>();

        MaxWork maxWork = new MaxWork(Runnable::run,
                2,
                () -> results.add("block"),
                () -> results.add("un block"));

        maxWork.execute(() -> results.add("execute"));

        assertThat(results, is( Arrays.asList("execute")));

    }
}
