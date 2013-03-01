package org.oddjob.beanbus.mega;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.Resetable;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.TrackingBusListener;
import org.oddjob.beanbus.drivers.IterableBusDriver;
import org.oddjob.state.ParentState;

public class MegaBeanBusTest extends TestCase {

	public void testSimpleLifecycle() {
		

		ArooaSession session = new OddjobSessionFactory().createSession();

		List<String> destination = new ArrayList<String>();
		
		IterableBusDriver<String> driver = new IterableBusDriver<String>();
		driver.setBeans(Arrays.asList("apple", "pear", "banana"));
		driver.setTo(destination);
		
		MegaBeanBus test = new MegaBeanBus();
		test.setArooaSession(session);
		test.setParts(0, driver);
		test.setParts(1, destination);
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
		
	}
	
	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/mega/MegaBeanBusExample.xml", 
				getClass().getClassLoader()));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
	
		@SuppressWarnings("unchecked")
		List<String> results = lookup.lookup("list.beans", List.class);
		
		assertEquals("Apple", results.get(0));
		assertEquals("Orange", results.get(1));
		assertEquals("Pear", results.get(2));
		assertEquals(3, results.size());
		
		oddjob.destroy();
	}
	
	public void testConfigurationSession() throws URISyntaxException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/mega/MegaBeanBusExample.xml", 
				getClass().getClassLoader()));
		oddjob.setExport("beans", new ArooaObject(
				Collections.EMPTY_LIST));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
	
		ConfigurationOwner test = (ConfigurationOwner) lookup.lookup("bus");

		ConfigurationSession session = test.provideConfigurationSession();
		
		ArooaDescriptor descriptor = session.getArooaDescriptor();

		ArooaClass cl = descriptor.getElementMappings().mappingFor(
				new ArooaElement(new URI("oddjob:beanbus"), "bean-copy"), 
						new InstantiationContext(ArooaType.COMPONENT, null));
		
		assertNotNull(cl);
		
		oddjob.destroy();
	}
	
	public static class NumberGenerator implements Runnable {
		
		private Collection<? super Integer> to;
		
		@Override
		public void run() {
			
			for (int i = 0; i < 100; ++i) {
				to.add(new Integer(i));
			}
			
		}
		
		public void setTo(Collection<? super Integer> to) {
			this.to = to;
		}
	}
	
	public static class OurDestination extends AbstractDestination<Integer> {

		int total;
		
		boolean started;
		
		boolean stopped;
		
		
		final TrackingBusListener busListener = new TrackingBusListener() {
			@Override
			public void busStarting(BusEvent event) throws BusCrashException {
				started = true;
				total = 0;
			}
			@Override
			public void busStopping(BusEvent event) throws BusCrashException {
				stopped = true;
			}
		};
		
		@Override
		public boolean add(Integer e) {
			total = total + e.intValue();
			return true;
		}
		
		@Inject
		public void setBusConductor(BusConductor busConductor) {
			busListener.setBusConductor(busConductor);
		}
		
		public int getTotal() {
			return total;
		}
		
		public boolean isStarted() {
			return started;
		}
		
		public boolean isStopped() {
			return stopped;
		}
	}
	
	public void testWithNoBusConductor() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <bean-bus id='test'>" +
				"   <parts>" +
				"    <bean class='" + NumberGenerator.class.getName() + "'>" +
				"     <to><value value='${results}'/></to>" +
				"    </bean>" +
				"    <bean class='" + OurDestination.class.getName() + "' " +
				"          id='results'/>" +
				"   </parts>" +
				"  </bean-bus>" +
				" </job>" +
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(new Integer(4950), lookup.lookup(
				"results.total", Integer.class));
		assertEquals(Boolean.TRUE, lookup.lookup(
				"results.started", boolean.class));
		assertEquals(Boolean.TRUE, lookup.lookup(
				"results.stopped", boolean.class));
		
		Object test = lookup.lookup("test");
		((Resetable) test).hardReset();
		((Runnable) test).run();
		
		assertEquals(new Integer(4950), lookup.lookup(
				"results.total", Integer.class));
		
		oddjob.destroy();
	}
}
