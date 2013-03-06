package org.oddjob.beanbus.mega;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.beanbus.Destination;
import org.oddjob.beanbus.Outbound;
import org.oddjob.framework.ComponentWrapper;
import org.oddjob.framework.DefaultInvocationHandler;

public class OutboundStratagiesTest extends TestCase {
	
	public class AnOutbound extends AbstractFilter<String, String> {
		
		Collection<? super String> to;

		@Override
		protected String filter(String from) {
			return null;
		}
		
		@Override
		public void setTo(Collection<? super String> destination) {
			this.to = destination;
		}
	}
	
	public void testInstanceAlready() {
		
		OutboundStrategies test = new OutboundStrategies();
		
		AnOutbound outbound = new AnOutbound();
		
		Outbound<String> result = test.outboundFor(outbound, null);
		
		assertSame(outbound, result);
	}
	
	public static class MyOutbound {
		
		Collection<String> stuff;
		
		@Destination
		public void setSomeStuff(Collection<String> stuff) {
			this.stuff = stuff;
		}
	}
	
	public void testFromAnnotation() {
		
		OutboundStrategies test = new OutboundStrategies();
		
		MyOutbound outbound = new MyOutbound();
		
		ArooaSession session = new StandardArooaSession();
		
		session.getComponentPool().registerComponent(
				new ComponentTrinity(outbound, 
						outbound, new MockArooaContext()), null);
		
		Outbound<String> result = test.outboundFor(outbound, session);
	
		List<String> list = new ArrayList<String>();
		
		result.setTo(list);
		
		assertSame(list, outbound.stuff);
	}
	
	public void testWithProxy() {
		
//		Method[] ms = BusFilter.class.getDeclaredMethods();
//		for (Method m : ms) {
//			System.out.println(m.toString());
//		}
		
		
		AnOutbound outbound = new AnOutbound();
		
		DefaultInvocationHandler handler = new DefaultInvocationHandler();
		
		Class<?>[] interfaces = new Class[] { BusFilter.class };
		
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), 
				interfaces, handler);
		
		handler.initialise(new ComponentWrapper() {}, new Class[0], 
				outbound, interfaces);
		
		OutboundStrategies test = new OutboundStrategies();
		
		Outbound<String> result = test.outboundFor(proxy, 
				new StandardArooaSession());
	
		List<String> list = new ArrayList<String>();
		
		result.setTo(list);
		
		assertSame(list, outbound.to);
		
	}
}
