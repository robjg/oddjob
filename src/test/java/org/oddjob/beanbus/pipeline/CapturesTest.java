package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CapturesTest {

    @Test
    public void testToList() {

        List<List<Integer>> results = new ArrayList<>();

        Pipe<Integer> pipe = Captures.<Integer>toList().linkTo(results::add);

        pipe.accept(1);
        pipe.accept(2);
        pipe.accept(3);

        pipe.flush();

        assertThat(results, is(Arrays.asList(Arrays.asList(1, 2, 3))));
    }

    @Test
    public void testToSet() {

        List<Set<Integer>> results = new ArrayList<>();

        Pipe<Integer> pipe = Captures.<Integer>toSet().linkTo(results::add);

        pipe.accept(1);
        pipe.accept(2);
        pipe.accept(3);

        pipe.flush();

        assertThat(results, is(Arrays.asList(new HashSet<>(Arrays.asList(1, 2, 3)))));
    }
}
