package org.oddjob.framework.adapt.job;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.Stoppable;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class AsyncCallableTest {

    public static class OurAsyncCallable implements Callable<CompletableFuture<Integer>>, Stoppable {

        int result;

        CountDownLatch latch;

        @Override
        public CompletableFuture<Integer> call() throws Exception {

            latch = new CountDownLatch(1);

            CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

            new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                completableFuture.complete(result);
            }).start();

            return completableFuture;
        }

        public void setResult(int result) {
            this.result = result;
        }

        @Override
        public void stop() throws FailedToStopException {
            latch.countDown();
        }
    }

    @Test
    public void testAsyncCallableCompletes() throws InterruptedException, FailedToStopException {

        String xml = "<oddjob>" +
                "<job>" +
                "<bean class='" + OurAsyncCallable.class.getName() + "'/>" +
                "</job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        oddjob.run();

        states.checkNow();
        states.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);

        oddjob.stop();

        states.checkWait();
    }
}
