package org.oddjob.beanbus.drivers;

import org.junit.Test;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.io.StdoutType;
import org.oddjob.tools.ConsoleCapture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IterableBusDriverTest {

    private interface Food {

    }

    private interface Fruit extends Food {

    }

    private class Apple implements Fruit {

    }

    @Test
    public void testSimpleRun() {

        List<Apple> fruit = new ArrayList<Apple>();

        fruit.add(new Apple());
        fruit.add(new Apple());

        IterableBusDriver<Apple> test = new IterableBusDriver<Apple>();
        test.setValues(fruit);

        List<Food> results = new ArrayList<Food>();

        test.setTo(results::add);

        test.run();

        assertThat(results.size(), is(2));
    }

    private class BlockingIterable implements Iterable<String> {

        CountDownLatch iteratorStarted = new CountDownLatch(1);

        boolean interrupted;

        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {

                @Override
                public boolean hasNext() {
                    try {
                        synchronized (IterableBusDriverTest.this) {
                            iteratorStarted.countDown();
                            IterableBusDriverTest.this.wait(5000L);
                            assertThat("Shouldn't happen", false);
                        }
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                    return false;
                }

                @Override
                public String next() {
                    throw new RuntimeException("Unexpected!");
                }

                @Override
                public void remove() {
                    throw new RuntimeException("Unexpected!");
                }
            };
        }
    }

    @Test
    public void testBlockedIterator() throws InterruptedException {

        BlockingIterable beans = new BlockingIterable();
        IterableBusDriver<String> test = new IterableBusDriver<>();
        test.setValues(beans);
        test.setTo(b -> {
        });

        Thread t = new Thread(test);
        t.start();

        if (!beans.iteratorStarted.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Should finish by now");
        }

        synchronized (IterableBusDriverTest.this) {
            // Iterator must now be blocking
        }

        test.stop();

        t.join();

        assertThat(beans.interrupted, is(true));
    }

    @Test
    public void testStdOutConsumer() throws ArooaConversionException {

        IterableBusDriver test = new IterableBusDriver();
        test.setValues(Arrays.asList("apple", "orange", "pear"));

        ConsoleCapture results = new ConsoleCapture();
        try (ConsoleCapture.Close close = results.captureConsole()) {

            test.setTo(new StdoutType().toConsumer());

            test.run();
        }

        assertThat(results.getLines(),
                is(new String[] { "apple", "orange", "pear" }));
    }
}
