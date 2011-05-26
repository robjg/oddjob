package org.oddjob.input;

import java.util.Properties;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobInheritance;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.Resetable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.xml.XMLConfiguration;

public class InputJobTest extends TestCase {

	private class OurInputHandler implements InputHandler {
		
		@Override
		public Properties handleInput(InputRequest[] requests) {
			
			Properties properties = new Properties();
			
			properties.setProperty("favourite.fruit", "apples");
			
			return properties;
		}
	}
	
	public void testFullLifeCycle() throws ArooaPropertyException, ArooaConversionException {

		String xml =
			"<oddjob>" +
			" <job>" +
			"  <input id='input'/>" +
			" </job>" +
			"</oddjob>";
		
		OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
		ArooaSession session = sessionFactory.createSession();
		
		session.getPropertyManager().addPropertyLookup(new PropertyLookup() {
			
			@Override
			public String lookup(String propertyName) {
				assertEquals("favourite.fruit", propertyName);
				return "pears";
			}
		});
		
		Oddjob oddjob = new Oddjob();
		oddjob.setArooaSession(session);
		oddjob.setInheritance(OddjobInheritance.SHARED);
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.setInputHandler(new OurInputHandler());
		
		PropertyLookup lookup = session.getPropertyManager();
		
		assertEquals("pears", lookup.lookup("favourite.fruit"));
		
		oddjob.run();
		
		assertEquals("apples", lookup.lookup("favourite.fruit"));
		
		Resetable resetable = session.getBeanRegistry().lookup(
				"input", Resetable.class);
		
		resetable.hardReset();
		
		assertEquals("pears", lookup.lookup("favourite.fruit"));
		
		oddjob.run();
		
		assertEquals("apples", lookup.lookup("favourite.fruit"));
		
		oddjob.destroy();
		
		assertEquals("pears", lookup.lookup("favourite.fruit"));
	}
}
