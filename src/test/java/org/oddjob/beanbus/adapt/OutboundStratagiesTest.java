package org.oddjob.beanbus.adapt;

import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.deploy.ConfigurationDescriptorFactory;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.beanbus.Destination;
import org.oddjob.beanbus.Outbound;
import org.oddjob.framework.adapt.ComponentWrapper;
import org.oddjob.framework.adapt.DefaultInvocationHandler;

import java.lang.reflect.Proxy;
import java.util.function.Consumer;

import static org.mockito.Mockito.when;

public class OutboundStratagiesTest extends OjTestCase {

    public static class AnOutbound extends AbstractFilter<String, String> {

        Consumer<? super String> to;

        @Override
        protected String filter(String from) {
            return null;
        }

        @Override
        public void setTo(Consumer<? super String> destination) {
            this.to = destination;
        }
    }

    @Test
    public void testInstanceAlready() {

        OutboundStrategies test = new OutboundStrategies();

        AnOutbound outbound = new AnOutbound();

        Outbound<String> result = test.outboundFor(outbound, null);

        assertSame(outbound, result);
    }

    public static class MyOutbound {

        Consumer<String> stuff;

        @Destination
        public void setSomeStuff(Consumer<String> stuff) {
            this.stuff = stuff;
        }
    }

    @Test
    public void testFromAnnotation() {

        OutboundStrategies test = new OutboundStrategies();

        MyOutbound outbound = new MyOutbound();

        ArooaSession session = new StandardArooaSession();

        session.getComponentPool().registerComponent(
                new ComponentTrinity(outbound,
                                     outbound, new MockArooaContext()), null);

        Outbound<String> result = test.outboundFor(outbound, session);

        Consumer<String> to = t -> {};

        result.setTo(to);

        assertSame(to, outbound.stuff);
    }


    public static class IndependentOutbound {

        Consumer<String> stuff;

        public void setStuff(Consumer<String> stuff) {
            this.stuff = stuff;
        }
    }

    @Test
    public void testFromDescriptorAnnotation() {

        String descriptorXml =
                "<arooa:descriptor xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'" +
                        "       namespace='http://rgordon.co.uk/test'>" +
                        " <components>" +
                        "  <arooa:bean-def element='my-bean'" +
                        "         className='" + IndependentOutbound.class.getName() + "'>" +
                        "   <properties>" +
                        "    <arooa:property name='stuff' " +
                        "           annotation='org.oddjob.beanbus.Destination'/>" +
                        "   </properties>" +
                        "  </arooa:bean-def>" +
                        " </components>" +
                        "</arooa:descriptor>";

        ArooaDescriptor descriptor =
                new ConfigurationDescriptorFactory(
                        new XMLConfiguration("XML", descriptorXml)
                ).createDescriptor(getClass().getClassLoader());

        OutboundStrategies test = new OutboundStrategies();

        IndependentOutbound outbound = new IndependentOutbound();

        ArooaSession session = new StandardArooaSession(descriptor);

        session.getComponentPool().registerComponent(
                new ComponentTrinity(outbound,
                                     outbound, new MockArooaContext()), null);

        Outbound<String> result = test.outboundFor(outbound, session);

        assertNotNull(result);

        Consumer<String> to = t -> {};

        result.setTo(to);

        assertSame(to, outbound.stuff);
    }

    @Test
    public void testWithProxy() {

//		Method[] ms = BusFilter.class.getDeclaredMethods();
//		for (Method m : ms) {
//			System.out.println(m.toString());
//		}


        AnOutbound outbound = new AnOutbound();

        DefaultInvocationHandler handler = new DefaultInvocationHandler();

        Class<?>[] interfaces = new Class[]{BusFilter.class};

        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(),
                                              interfaces, handler);

        ComponentWrapper componentWrapper =
                Mockito.mock(ComponentWrapper.class);
        when(componentWrapper.loggerName()).thenReturn("SomeLogger");

        handler.initialise(componentWrapper, new Class[0],
                           outbound, interfaces);

        OutboundStrategies test = new OutboundStrategies();

        Outbound<String> result = test.outboundFor(proxy,
                                                   new StandardArooaSession());

        Consumer<String> to = t -> {};

        result.setTo(to);

        assertSame(to, outbound.to);
    }
}
