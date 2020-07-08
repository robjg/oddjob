package org.oddjob.jmx.client;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.xml.XMLConfiguration;

public class ResourceFactoryProviderTest extends OjTestCase {

    @Test
    public void testProvideFactories() {

        ArooaSession session = new OddjobSessionFactory().createSession();

        ResourceFactoryProvider test = new ResourceFactoryProvider(
                session);

        ClientInterfaceHandlerFactory<?>[] handlerFactories
                = test.getHandlerFactories();

        assertEquals(11, handlerFactories.length);
    }

    public static class HandlerCounter implements Runnable, ArooaSessionAware {

        int count;

        ArooaSession session;

        public void setArooaSession(ArooaSession session) {
            this.session = session;
        }

        public void run() {

            ResourceFactoryProvider test = new ResourceFactoryProvider(
                    session);

            ClientInterfaceHandlerFactory<?>[] handlerFactories
                    = test.getHandlerFactories();

            count = handlerFactories.length;
        }

        public int getCount() {
            return count;
        }
    }

    @Test
    public void testFactoriesInOddjob() throws ArooaConversionException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean id='x' class='" + HandlerCounter.class.getName() + "'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        int count = new OddjobLookup(oddjob).lookup("x.count", int.class);

        assertEquals(11, count);
    }
}
