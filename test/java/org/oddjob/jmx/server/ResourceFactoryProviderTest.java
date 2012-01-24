package org.oddjob.jmx.server;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.xml.XMLConfiguration;

public class ResourceFactoryProviderTest extends TestCase {

	public void testProvideFactories() throws ArooaParseException {

		ArooaSession session = new OddjobSessionFactory().createSession();
		
		ResourceFactoryProvider test = new ResourceFactoryProvider(
				session);
		
		ServerInterfaceHandlerFactory<?, ?>[] handlerFactories
			 = test.getHandlerFactories();

		assertEquals(10, handlerFactories.length);
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
			
			ServerInterfaceHandlerFactory<?, ?>[] handlerFactories
				 = test.getHandlerFactories();

			count = handlerFactories.length;
		}
		
		public int getCount() {
			return count;
		}
	}
	
	public void testFactoriesInOddjob() throws ArooaParseException, ArooaConversionException {

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
		
		assertEquals(10, count);
	}
}
