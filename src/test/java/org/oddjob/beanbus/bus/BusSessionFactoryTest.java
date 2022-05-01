package org.oddjob.beanbus.bus;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ClassResolverClassLoader;
import org.oddjob.arooa.standard.StandardArooaSession;

import java.util.function.Consumer;

public class BusSessionFactoryTest {

    @Test
    public void testCreateSessionFactory() {

        BusSessionFactory test = new BusSessionFactory();

        ArooaSession existingSession = new StandardArooaSession();

        ArooaDescriptor existingDescriptor =
                existingSession.getArooaDescriptor();

        ClassLoader classLoader = new ClassResolverClassLoader(
                existingDescriptor.getClassResolver());

        ArooaSession session = test.createSession(
                existingSession, classLoader);

        Consumer<String> someConsumer = s -> {
        };

        Object resolved = session.getComponentProxyResolver()
                .resolve(someConsumer, session);

        MatcherAssert.assertThat(resolved, Matchers.instanceOf(Stateful.class));
    }
}
