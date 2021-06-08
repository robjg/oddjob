package org.oddjob.beanbus.mega;

import org.apache.commons.beanutils.DynaBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Describable;
import org.oddjob.Iconic;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.beanbus.*;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;

import java.util.Map;
import java.util.function.Consumer;

public class CollectionProxyGeneratorTest extends OjTestCase {

    public static class OurDestination extends AbstractDestination<String> {

        private int number;

        @Override
        public void accept(String s) {

        }

        public String getFruit() {
            return "Apple";
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }


    Object proxy;

    @Before
    public void setUp() throws Exception {
        CollectionProxyGenerator<String> test =
                new CollectionProxyGenerator<>();

        proxy = test.generate(
                new OurDestination(), getClass().getClassLoader());

        ((ArooaSessionAware) proxy).setArooaSession(new StandardArooaSession());
    }

    @After
    public void tearDown() throws Exception {
        ((ArooaLifeAware) proxy).destroy();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCollection() {

        assertTrue(proxy instanceof Consumer);

        BasicBeanBus<String> bus = new BasicBeanBus<>();
        bus.getBusConductor().addBusListener(new BusListenerAdapter() {
            @Override
            public void busStarting(BusEvent event) throws BusCrashException {
                throw new BusCrashException("Crash");
            }
        });

        ((BusPart) proxy).prepare(bus.getBusConductor());

        try {
            bus.startBus();
            fail("Should Throw Exception");
        } catch (BusCrashException e) {
            assertEquals("Crash", e.getMessage());
        }

        try {
            ((Consumer<String>) proxy).accept("apples");
            fail("Should Throw Exception");
        } catch (RuntimeException e) {
            assertEquals("Crash",
                    e.getCause().getCause().getCause().getMessage());
        }

    }

    @Test
    public void testDescribable() {

        assertTrue(proxy instanceof Describable);

        Map<String, String> description = ((Describable) proxy).describe();

        assertNotNull(description);

        assertEquals("Apple", description.get("fruit"));
    }


    @Test
    public void testDynaBean() {

        assertTrue(proxy instanceof DynaBean);

        DynaBean dynaBean = ((DynaBean) proxy);

        assertEquals("Apple", dynaBean.get("fruit"));

        dynaBean.set("number", 3);

        assertEquals(3, dynaBean.get("number"));
    }

    static class OurIconListener implements IconListener {

        String iconId;

        @Override
        public void iconEvent(IconEvent e) {
            iconId = e.getIconId();
        }
    }

    @Test
    public void testIconicBusPart() throws BusCrashException {

        assertTrue(proxy instanceof Iconic);

        Iconic iconic = (Iconic) proxy;

        OurIconListener listener = new OurIconListener();

        iconic.addIconListener(listener);

        assertEquals(CollectionWrapper.INACTIVE, listener.iconId);

        BusPart busPart = (BusPart) proxy;

        BasicBeanBus<String> bus = new BasicBeanBus<>();

        busPart.prepare(bus.getBusConductor());

        assertEquals(CollectionWrapper.INACTIVE, listener.iconId);

        bus.startBus();

        assertEquals(CollectionWrapper.ACTIVE, listener.iconId);

        bus.stopBus();

        assertEquals(CollectionWrapper.INACTIVE, listener.iconId);

    }

}
