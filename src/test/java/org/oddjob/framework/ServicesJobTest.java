package org.oddjob.framework;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.registry.Services;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class ServicesJobTest extends OjTestCase {

	interface Snack {
		// A marker interface only.		
	}
	interface SnackProvider {
		
		public Snack provideSnack();
	}
	
	public static class Cafe implements SnackProvider {
			
		@Override
		public Snack provideSnack() {
			return new Snack() {
				@Override
				public String toString() {
					return "Green Eggs and Ham";
				}
			};
		}
		
		@Override
		public String toString() {
			return "MyCafe";
		}
		
	}
	
   @Test
	public void testServiceRegisteredAndRetrieved() {
		
		ServicesJob test = new ServicesJob();
		
		ServicesJob.ServiceDefinition def = 
			new ServicesJob.ServiceDefinition();
		def.setService(new Cafe());
		
		test.setRegisteredServices(0, def);
		
		Services services = test.getServices();
		
		String serviceName = services.serviceNameFor(SnackProvider.class, null);
		
		assertNotNull(serviceName);
		
		Object service = services.getService(serviceName);
		
		assertEquals(Cafe.class, service.getClass());
	}
	
   @Test
	public void testServiceLookup() {
		
		String xml = 
			"<oddjob><job>" +
			" <services id='my-services'>" +
			"  <registeredServices>" +
			"   <is>" +
			"    <service>" +
			"     <bean class='" + Cafe.class.getName() + "'/>" +
			"    </service>" +
			"    <qualifier>" +
			"     <value value='non-veggie'/>" +
			"    </qualifier>" +
			"   </is>" +
			"  </registeredServices>" +
			" </services>" +
			"</job></oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		Object service = new OddjobLookup(oddjob).lookup(
				"my-services.services.service(MyCafe;non-veggie)");
		
		assertEquals(Cafe.class, service.getClass());
		
		oddjob.destroy();
	}
}
