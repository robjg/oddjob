package org.oddjob.util;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class EtcTest {

    @Test
    public void whenStackTraceThenStringReadable() throws InterruptedException {
        AtomicReference<String> result = new AtomicReference<>();

        Thread t = new Thread(() -> {
            result.set(Etc.toTabbedString(aMethod()));
        });

        t.start();
        t.join();

//        System.out.println(result);

        assertThat(result.get(), containsString("org.oddjob.util.EtcTest.anotherMethod(EtcTest.java:"));

    }

    StackTraceElement[] aMethod() {
        return anotherMethod();
    }
    StackTraceElement[] anotherMethod() {
        return Thread.currentThread().getStackTrace();
    }
}