package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CapturesTest {

    @Test
    public void testList() {

        List<List<Integer>> results = new ArrayList<>();

        Pipe<Integer> pipe = Captures.<Integer>toList().linkTo(results::add);

        pipe.accept(1);
        pipe.accept(2);
        pipe.accept(3);

        pipe.flush();


    }
}
