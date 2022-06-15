package org.oddjob.framework.adapt.async;


import org.junit.Test;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.AsyncJob;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class MethodAsyncHelperTest {

    public static class MyAsyncTask {

        public CompletableFuture<Integer> go() {
            return CompletableFuture.completedFuture(1);
        }

    }

    @Test
    public void testMethodCalledAndSetsCallback() throws NoSuchMethodException {

        MyAsyncTask task = new MyAsyncTask();

        AsyncJob asyncJob = MethodAsyncHelper.adapt(task,
                task.getClass().getMethod("go"),
                new StandardArooaSession())
                .orElseThrow(() -> new IllegalStateException("Shouldn't fail"));

        AtomicInteger state = new AtomicInteger();
        AtomicReference<Exception> exception = new AtomicReference<>();

        asyncJob.acceptCompletionHandle(state::set);
        asyncJob.acceptExceptionListener(exception::set);

        asyncJob.run();

        assertThat(state.get(), is(1));
        assertThat(exception.get(), nullValue());
    }

    public static class MyNaughtyAsyncTask {

        public CompletableFuture<Integer> go() {
            return CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Doh!");
            });
        }

    }

    @Test
    public void testMethodCalledAndSetsException() throws NoSuchMethodException, InterruptedException {

        MyNaughtyAsyncTask task = new MyNaughtyAsyncTask();

        AsyncJob asyncJob = MethodAsyncHelper.adapt(task,
                        task.getClass().getMethod("go"),
                        new StandardArooaSession())
                .orElseThrow(() -> new IllegalStateException("Shouldn't fail"));

        AtomicReference<Integer> state = new AtomicReference<>();
        BlockingQueue<Exception> exception = new LinkedBlockingQueue<>();

        asyncJob.acceptCompletionHandle(state::set);
        asyncJob.acceptExceptionListener(exception::add);

        asyncJob.run();

        assertThat(state.get(), nullValue());
        assertThat(exception.poll(5, TimeUnit.SECONDS).getMessage(), is("java.lang.RuntimeException: Doh!"));
    }
}