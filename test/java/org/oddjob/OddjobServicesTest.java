package org.oddjob;

import org.junit.Test;

import javax.inject.Inject;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.input.InputHandler;
import org.oddjob.state.ParentState;

public class OddjobServicesTest extends OjTestCase {

	public static class MyOddjobAware implements Runnable {
		OddjobServices oddjobServices;

		@Inject
		public void setOddjobServices(OddjobServices oddjobServices) {
			this.oddjobServices = oddjobServices;
		}
		
		public OddjobServices getOddjobServices() {
			return oddjobServices;
		}
		
		public void run() {
		}
	}
	
   @Test
	public void testDefaultServices() throws ArooaConversionException {
	
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean class='" + MyOddjobAware.class.getName() + 
			"' id='mine'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob test = new Oddjob();
		test.setOddjobServices(new MyOddjobServices());
		test.setConfiguration(new XMLConfiguration("XML", xml));
		test.run();
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
		OddjobServices result = new OddjobLookup(
				test).lookup("mine.oddjobServices", OddjobServices.class);
		
		assertNotNull(result);
		
		test.destroy();
	}
	
	class MyOddjobServices extends MockOddjobServices {
		
		@Override
		public Object getService(String serviceName) {
			assertEquals(ODDJOB_SERVICES, serviceName);
			return this;
		}
		
		@Override
		public String serviceNameFor(Class<?> theClass, String flavour) {
			assertEquals(OddjobServices.class, theClass);
			return ODDJOB_SERVICES;
		}
		
		@Override
		public ClassLoader getClassLoader() {
			return getClass().getClassLoader();
		}
		
		@Override
		public OddjobExecutors getOddjobExecutors() {
			return null;
		}
		
		@Override
		public InputHandler getInputHandler() {
			return null;
		}
	}
	
   @Test
	public void testMyServices() throws ArooaConversionException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean class='" + MyOddjobAware.class.getName() + "' id='mine'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));
		test.setOddjobServices(new MyOddjobServices());
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
		OddjobServices result = new OddjobLookup(
				test).lookup("mine.oddjobServices", OddjobServices.class);
		
		assertNotNull(result);
		
		test.destroy();
	}
	
   @Test
	public void testNestedServices() throws ArooaConversionException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <oddjob id='nested'>" +
			"   <configuration>" +
			"      <xml>" +
			"       <oddjob>" +
			"        <job>" +
			"         <bean class='" + MyOddjobAware.class.getName() + "' id='mine'/>" +
			"        </job>" +
			"       </oddjob>" +
			"      </xml>" +
			"   </configuration>" +
			"  </oddjob>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));
		test.setOddjobServices(new MyOddjobServices());
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
		OddjobServices result = new OddjobLookup(
				test).lookup("nested/mine.oddjobServices", OddjobServices.class);
		
		assertNotNull(result);
		
		test.destroy();
	}
}
