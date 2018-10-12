package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FoldsTest {

    @Test
    public void testCount() {

        Processor<String, Long> processor = SyncPipeline.<String>begin()
                .to(Folds.count())
                .create();

        processor.accept("a");
        processor.accept("b");
        processor.accept("c");

        Long result = processor.complete();

        assertThat(result, is(3L));
    }
}
