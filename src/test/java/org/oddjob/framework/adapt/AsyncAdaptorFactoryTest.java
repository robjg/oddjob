package org.oddjob.framework.adapt;


import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AsyncAdaptorFactoryTest {

    static class OurCallable implements Callable<String> {

        @Override
        public String call() throws Exception {
            return null;
        }
    }

    static class OurOtherThingsAndCallable implements Callable<CompletableFuture<String>>, Runnable {

        @Override
        public void run() {

        }

        @Override
        public CompletableFuture<String> call() throws Exception {
            return null;
        }
    }

    @Test
    public void testParameterTypeAssumptions() {

        class Foo implements Callable<CompletableFuture<String>> {
            @Override
            public CompletableFuture<String> call() throws Exception {
                return null;
            }
        }

        Callable<CompletableFuture<String>> foo = new Foo();

        Type[] interfaces = foo.getClass().getGenericInterfaces();

        assertThat(interfaces.length, is(1));

        Type theInterface = interfaces[0];

        assertThat(theInterface, instanceOf(ParameterizedType.class));

        ParameterizedType parameterizedType = (ParameterizedType) theInterface;

        assertThat(parameterizedType.getRawType(), is(Callable.class));

        Type firstParam = parameterizedType.getActualTypeArguments()[0];

        assertThat(firstParam, instanceOf(ParameterizedType.class));
        assertThat(firstParam, not(instanceOf(Class.class)));
    }

    @Test
    public void testParameterisedType() {

        assertThat(AsyncAdaptorFactory.getCallableType(new OurCallable()),
                is(String.class));

        assertThat(AsyncAdaptorFactory.getCallableType(new OurOtherThingsAndCallable()),
                is(CompletableFuture.class));
    }
}