package org.oddjob.framework.adapt.job;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;

import java.util.Optional;
import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.is;

public class CallableProxyGeneratorTest extends OjTestCase {

    public interface MyInterface {

    }

    public static class MyJob implements Callable<Integer>, MyInterface {
        @Override
        public Integer call() throws Exception {
            return 0;
        }
    }

    @Test
    public void testAProxyImplementsAllInterfaces() {

        ArooaSession session = new StandardArooaSession();

        MyJob callable = new MyJob();

        Optional<JobAdaptor> jobAdaptor = new JobStrategies().adapt(callable, session);
        assertThat(jobAdaptor.isPresent(), is(true));

        Object proxy = new JobProxyGenerator().generate(
                jobAdaptor.get(),
                getClass().getClassLoader());

        assertTrue(proxy instanceof MyInterface);
    }
}
