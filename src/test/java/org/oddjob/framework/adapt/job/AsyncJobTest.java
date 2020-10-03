package org.oddjob.framework.adapt.job;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.adapt.AcceptCompletionHandle;
import org.oddjob.framework.adapt.AcceptExceptionListener;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.state.ParentState;
import org.oddjob.structural.OddjobChildException;
import org.oddjob.tools.StateSteps;

import java.beans.ExceptionListener;
import java.util.concurrent.CountDownLatch;
import java.util.function.IntConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AsyncJobTest {

    public static class MyJob implements Runnable {

        int result;

        CountDownLatch latch;

        IntConsumer completionHandler;

        @Override
        public void run() {

            latch = new CountDownLatch(1);

            new Thread(() ->{
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                completionHandler.accept(result);
            }).start();
        }

        @AcceptCompletionHandle
        public void acceptCompletion(IntConsumer handler) {
            this.completionHandler = handler;
        }

        public void setResult(int result) {
            this.result = result;
        }

        @Stop
        public void stop() {
            latch.countDown();
        }
    }

    @Test
    public void testCompleteWithAnnotation() throws InterruptedException, FailedToStopException {

        String xml = "<oddjob>" +
                "<job>" +
                "<bean class='" + MyJob.class.getName() + "'/>" +
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

    @Test
    public void testIncompleteWithAnnotation() throws InterruptedException, FailedToStopException {

        String xml = "<oddjob>" +
                "<job>" +
                "<bean class='" + MyJob.class.getName() + "' result='1'/>" +
                "</job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        oddjob.run();

        states.checkNow();
        states.startCheck(ParentState.ACTIVE, ParentState.INCOMPLETE);

        oddjob.stop();

        states.checkWait();
    }


    public static class MyBadJob implements Runnable {

        CountDownLatch latch;

        ExceptionListener exceptionListener;

        @Override
        public void run() {

            latch = new CountDownLatch(1);

            new Thread(() ->{
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                exceptionListener.exceptionThrown(new Exception("Uh Oh"));
            }).start();
        }

        @AcceptExceptionListener
        public void setExceptionListener(ExceptionListener exceptionListener) {
            this.exceptionListener = exceptionListener;
        }

        @Stop
        public void stop() {
            latch.countDown();
        }
    }

    @Test
    public void testExceptionWithAnnotation() throws InterruptedException, FailedToStopException {

        String xml = "<oddjob>" +
                "<job>" +
                "<bean class='" + MyBadJob.class.getName() + "'/>" +
                "</job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        oddjob.run();

        states.checkNow();
        states.startCheck(ParentState.ACTIVE, ParentState.EXCEPTION);

        oddjob.stop();

        states.checkWait();

        OddjobChildException ep = (OddjobChildException) oddjob.lastStateEvent().getException();

        assertThat(ep.getCause().getMessage(), is("Uh Oh"));
    }

}
