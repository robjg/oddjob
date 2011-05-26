package org.oddjob.persist;

import java.io.Serializable;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;

public class PersistAssumptionsTest extends TestCase {

	public static class Thing implements Runnable, Serializable {
		private static final long serialVersionUID = 2010121300L;
		
		private Value stuff;

		public Value getStuff() {
			return stuff;
		}

		public void setStuff(Value stuff) {
			this.stuff = stuff;
		}
		
		@Override
		public void run() {
		}
	}
	
	public static class Value implements Serializable {
		private static final long serialVersionUID = 2010121300L;
		
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public static void testValues() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
			"<oddjob id='this'>" +
			" <job>" +
			"  <bean class='" + Thing.class.getName() + "' id='t'>" +
			"   <stuff>" +
			"    <is value='${this.args[0]}' id='v'/>" +
			"   </stuff>" +
			"  </bean>" +
			" </job>" + 
			"</oddjob>";
		 
		
		MapPersister persister = new MapPersister();
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setPersister(persister);
		oddjob1.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob1.setArgs(new String[] { "apples" });
		oddjob1.run();
		
		assertEquals("apples", new OddjobLookup(oddjob1).lookup("t.stuff.value"));
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setPersister(persister);
		oddjob2.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob2.setArgs(new String[] { "oranges" });

		oddjob2.load();
		
		assertEquals("apples", new OddjobLookup(oddjob2).lookup("t.stuff.value"));
		
		new OddjobLookup(oddjob2).lookup("t", Resetable.class).hardReset();
		
		oddjob2.run();
		
		assertEquals("oranges", new OddjobLookup(oddjob2).lookup("t.stuff.value"));
	}
	
}
