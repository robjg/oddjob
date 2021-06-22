package org.oddjob.beanbus;

import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.OjTestCase;

import java.beans.ExceptionListener;
import java.io.Flushable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class SimpleBusConductorTest extends OjTestCase {

    interface FullLifecycleComponent extends Runnable, Flushable, AutoCloseable, ExceptionListener {

    }


    @Test
    public void testStandardLifecycle() throws Exception {

        FullLifecycleComponent component = mock(FullLifecycleComponent.class);

        SimpleBusConductor test = new SimpleBusConductor(component);

        test.run();

        verify(component, times(1)).run();

        test.close();

        verify(component, times(1)).flush();
        verify(component, times(1)).close();
    }


    @Test
    public void testCrashTheBusLifecycle() throws Exception {

        FullLifecycleComponent component = mock(FullLifecycleComponent.class);

        SimpleBusConductor test = new SimpleBusConductor(component);

        test.run();

        verify(component, times(1)).run();

        Exception e = new Exception("Doh");
        test.actOnBusCrash(e);

        verify(component, times(1)).exceptionThrown(e);
        verify(component, never()).flush();
        verify(component, times(1)).close();
    }


    @Test
    public void testCleaningLifecycle() throws Exception {

        FullLifecycleComponent component = mock(FullLifecycleComponent.class);

        SimpleBusConductor test = new SimpleBusConductor(component);

        test.run();

        verify(component, times(1)).run();

        test.flush();

        verify(component, times(1)).flush();

        test.close();

        verify(component, times(2)).flush();
        verify(component, times(1)).close();

    }

    @Test
    public void testCrashOnStarting() throws Exception {

        RuntimeException e = new RuntimeException("Bang!");

        Runnable badComponent = mock(Runnable.class);
        Mockito.doThrow(e).when(badComponent).run();

        FullLifecycleComponent component = mock(FullLifecycleComponent.class);

        SimpleBusConductor test = new SimpleBusConductor(badComponent, component);

        try {
            test.run();
            fail("Should fail");
        } catch (RuntimeException e2) {
            assertEquals("Bang!", e2.getMessage());
        }

        verify(component, times(1)).run();
        verify(component, times(1)).exceptionThrown(e);
        verify(component, never()).flush();
        verify(component, times(1)).close();
    }

    @Test
    public void testCrashedByListenerWhenCleaning() throws Exception {

        RuntimeException e = new RuntimeException("Bang!");

        Flushable badComponent = mock(Flushable.class);
        Mockito.doThrow(e).when(badComponent).flush();

        FullLifecycleComponent component = mock(FullLifecycleComponent.class);

        SimpleBusConductor test = new SimpleBusConductor(badComponent, component);

        test.run();

        test.flush();

        verify(component, times(1)).run();
        verify(component, times(1)).exceptionThrown(e);
        verify(component, never()).flush();
        verify(component, times(1)).close();
    }

    @Test
    public void CloseWhileStarting() throws Exception {

        CountDownLatch closeLatch = new CountDownLatch(1);
        CountDownLatch startedLatch = new CountDownLatch(1);

        FullLifecycleComponent component = mock(FullLifecycleComponent.class);
        doAnswer(invocation -> {
            startedLatch.countDown();
            if (!closeLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Should count down");
            }
            return null;
        }).when(component).run();
        doAnswer(invocation -> {
            closeLatch.countDown();
            return null;
        }).when(component).close();

        SimpleBusConductor test = new SimpleBusConductor(component);

        Thread t = new Thread(test);
        t.start();

        if (!startedLatch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Should count down");
        }

        test.close();

        t.join();

        verify(component, times(1)).run();
        verify(component, never()).exceptionThrown(any());
        verify(component, times(1)).flush();
        verify(component, times(1)).close();
    }
}
