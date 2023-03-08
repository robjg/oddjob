package org.oddjob;

import org.junit.Test;
import org.oddjob.arooa.*;
import org.oddjob.arooa.convert.*;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.life.MockComponentPersister;
import org.oddjob.arooa.registry.*;
import org.oddjob.arooa.standard.MockPropertyLookup;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.tools.OddjobTestHelper;

public class OddjobArooaSessionTest extends OjTestCase {

    private static class OurDescriptor extends MockArooaDescriptor {

        @Override
        public ConversionProvider getConvertletProvider() {
            return registry -> registry.register(String.class, Integer.class,
                    from -> 42);
        }

        @Override
        public ElementMappings getElementMappings() {
            return null;
        }
    }

    @Test
    public void testConversions()
            throws ArooaConversionException,
            InvalidIdException {

        OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
        sessionFactory.setDescriptorFactory(
                classLoader -> new OurDescriptor());

        ArooaSession test = sessionFactory.createSession();

        ArooaConverter converter = test.getTools().getArooaConverter();

        assertEquals(Integer.valueOf(42), converter.convert(
                "forty two", Integer.class));

        test.getBeanRegistry().register("apples", "A String");

        Integer i = test.getBeanRegistry().lookup("apples", Integer.class);

        assertEquals(Integer.valueOf(42), i);
    }


    private static class OuterSession extends MockArooaSession {
        ComponentPool componentPool = new SimpleComponentPool();

        BeanRegistry beanRegistry = new SimpleBeanRegistry();

        @Override
        public ArooaDescriptor getArooaDescriptor() {
            return new MockArooaDescriptor() {
                @Override
                public ConversionProvider getConvertletProvider() {
                    return new DefaultConversionProvider();
                }

                @Override
                public ElementMappings getElementMappings() {
                    return null;
                }
            };
        }

        @Override
        public ComponentProxyResolver getComponentProxyResolver() {
            return null;
        }

        @Override
        public ComponentPersister getComponentPersister() {
            return null;
        }

        @Override
        public ArooaTools getTools() {
            return new StandardTools();
        }

        @Override
        public ComponentPool getComponentPool() {
            return componentPool;
        }

        @Override
        public BeanRegistry getBeanRegistry() {
            return beanRegistry;
        }
    }


    @Test
    public void testNestedConversion() throws NoConversionAvailableException, ConversionFailedException {

        OuterSession session = new OuterSession();

        Oddjob oddjob = new Oddjob();

        OddjobTestHelper.register(oddjob, session, null);

        OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
        sessionFactory.setInherit(OddjobInheritance.NONE);
        sessionFactory.setExistingSession(session);
        sessionFactory.setDescriptorFactory(
                classLoader -> new OurDescriptor());

        ArooaSession test = sessionFactory.createSession(oddjob);

        ArooaConverter converter = test.getTools().getArooaConverter();

        Number number = converter.convert("2", Integer.class);

        assertEquals(42, number);
    }

    @Test
    public void testNestedPropertyManager() {

        StandardArooaSession outerSession = new StandardArooaSession();

        OddjobSessionFactory test = new OddjobSessionFactory();
        test.setExistingSession(outerSession);
        // Test Default behaviour.
        test.setInherit(null);

        ArooaSession session = test.createSession();

        outerSession.getPropertyManager().addPropertyLookup(new MockPropertyLookup() {

            @Override
            public String lookup(String propertyName) {
                assertEquals("fruit", propertyName);
                return "apple";
            }
        });

        assertEquals("apple", session.getPropertyManager().lookup("fruit"));
    }

    private static class OurPersister extends MockComponentPersister
            implements OddjobPersister {

        @Override
        public ComponentPersister persisterFor(final String id) {
            return new MockComponentPersister() {
                @Override
                public String toString() {
                    return "OurPersister " + id;
                }
            };
        }
    }

    @Test
    public void testNestedComponentPersisterNoOddjobId() {

        StandardArooaSession outerSession = new StandardArooaSession() {
            public ComponentPersister getComponentPersister() {
                return new OurPersister();
            }
        };

        Oddjob oddjob = new Oddjob();

        OddjobSessionFactory test = new OddjobSessionFactory();
        test.setExistingSession(outerSession);

        ArooaSession session = test.createSession(oddjob);

        assertNull(session.getComponentPersister());
    }

    @Test
    public void testNestedComponentPersister() {

        StandardArooaSession outerSession = new StandardArooaSession() {
            public ComponentPersister getComponentPersister() {
                return new OurPersister();
            }
        };

        Oddjob oddjob = new Oddjob();

        outerSession.getBeanRegistry().register("stuff", oddjob);

        OddjobSessionFactory test = new OddjobSessionFactory();
        test.setExistingSession(outerSession);

        ArooaSession session = test.createSession(oddjob);

        assertEquals("OurPersister stuff",
                session.getComponentPersister().toString());
    }
}
