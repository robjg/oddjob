/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.values.properties;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;

/**
 * Test for PropertiesType.
 * 
 * @author rob
 *
 */
public class PropertiesTypeTest extends TestCase {

	/**
	 * Test a simple get and a set.
	 * 
	 * @throws Exception
	 */
	public void testSimpleSetGet() throws Exception {
		
		PropertiesType test = new PropertiesType();
		
		test.setValues("a.b.c", "Test");
		
		Properties results = test.toProperties();
		
		assertEquals("Test", results.getProperty("a.b.c"));
		
		assertEquals("Test", results.get("a.b.c"));
	}
		
	public static class MyComp extends SimpleJob {
		Properties props;
		
		public void setProps(Properties props) {
			this.props = props;
		}
		
		@Override
		protected int execute() throws Throwable {
			return 0;
		}
	}

	/**
	 * Test that properties can be set from each other.
	 */
	public void testSetInOddjob() {
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='mycomp' class='" + MyComp.class.getName() + "'>" +
			"    <props>" +
			"     <properties>" +
			"      <values>" +
			"       <value key='snack.favourite' value='apples'/>" +
			"       <value key='fruit.favourite' value='${snack.favourite}'/>" +
			"      </values>" +
			"     </properties>" +
			"    </props>" +
			"  </bean>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		
		MyComp myComp = (MyComp) new OddjobLookup(oj).lookup("mycomp");

		assertNotNull(myComp);
		
		assertEquals("apples", myComp.props.get("fruit.favourite"));
	}
	
	public static class ThingWithGetters {
		
		public Long getLong() {
			return new Long(1234567890L);
		}
	}
	
	public void testNonStringValue() throws IOException, ArooaConversionException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <bean id='thing' class='" + ThingWithGetters.class.getName() + "'/>" +
			"    <bean id='mycomp' class='" + MyComp.class.getName() + "'>" +
			"     <props>" +
			"      <properties>" +
			"       <values>" +
			"        <value key='thing.long' value='${thing.long}'/>" +
			"       </values>" +
			"      </properties>" +
			"     </props>" +
			"    </bean>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		
		MyComp myComp = (MyComp) new OddjobLookup(oj).lookup("mycomp");

		assertNotNull(myComp);
		
		assertEquals("1234567890", myComp.props.getProperty("thing.long"));
	}
	
	
	public void testMergeTwoPropertySetsWhereSecondReferencesFirst() {
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='mycomp' class='" + MyComp.class.getName() + "'>" +
			"    <props>" +
			"     <properties>" +
			"      <sets>" +
			"       <properties>" +
			"        <values>" +
			"         <value key='snack.favourite' value='apples'/>" +
			"        </values>" +
			"       </properties>" +
			"       <properties>" +
			"        <values>" +
			"         <value key='fruit.favourite' value='${snack.favourite}'/>" +
			"        </values>" +
			"       </properties>" +
			"      </sets>" +
			"     </properties>" +
			"    </props>" +
			"  </bean>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		
		MyComp myComp = (MyComp) new OddjobLookup(oj).lookup("mycomp");

		assertNotNull(myComp);
		
		assertEquals("apples", myComp.props.get("snack.favourite"));
		assertEquals("apples", myComp.props.get("fruit.favourite"));
	}
	
	public void testMergeFiles() {
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='mycomp' class='" + MyComp.class.getName() + "'>" +
			"    <props>" +
			"     <properties substitute='true'>" +
			"      <sets>" +
			"       <properties>" +
			"        <input>" +
			"         <resource resource='org/oddjob/values/properties/PropertiesTypeTest1.properties'/>" +
			"        </input>" +
			"       </properties>" +
			"       <properties>" +
			"        <input>" +
			"         <resource resource='org/oddjob/values/properties/PropertiesTypeTest2.properties'/>" +
			"        </input>" +
			"       </properties>" +
			"      </sets>" +
			"     </properties>" +
			"    </props>" +
			"  </bean>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		
		MyComp myComp = (MyComp) new OddjobLookup(oj).lookup("mycomp");

		assertNotNull(myComp);
		
		assertEquals("apples", myComp.props.get("fruit.favourite"));
		assertEquals("apples", myComp.props.get("snack.favourite"));
	}
	
	public void testExtractAndPrefix() throws IOException, ArooaConversionException {
		
		Properties props = new Properties();
		
		props.setProperty("snack.junk.thing", "pizza");
		props.setProperty("snack.fruit.thing", "apple");
		
		PropertiesType test = new PropertiesType();
		
		test.setExtract("snack.fruit");
		test.setPrefix("snack.healthy");
		
		test.setSets(0, props);
		
		Properties results = test.toProperties();
	
		assertEquals(1, results.size());
		assertEquals("apple", results.getProperty("snack.healthy.thing"));
	}
	
	/**
	 * Test null values are empty properties.
	 */
	public void testNullValue() throws IOException, ArooaConversionException {
		
		PropertiesType test = new PropertiesType();
		test.setValues("foo", null);
		
		Properties results = test.toProperties();
		
		assertEquals("", results.getProperty("foo"));
	}
	
	/**
	 * Tracking down a bug where properties don't appear to be loaded in
	 * variables from an input buffer - still don't know why because this
	 * works.
	 * 
	 * @throws ArooaConversionException 
	 * @throws ArooaPropertyException 
	 */
	public void testPropertiesInVaraiblesFromBuffer() throws ArooaPropertyException, ArooaConversionException {
		
		URL url = getClass().getResource("PropertiesInVariables.xml");
		
		Properties properties = new Properties();
		properties.setProperty("favourite.fruit", "Apples");
		
		Oddjob oddjob = new Oddjob();		
		oddjob.setFile(new File(url.getFile()));
		oddjob.setProperties(properties);
		
		oddjob.run();
		
		Properties results = new OddjobLookup(oddjob).lookup("vars.props",
				Properties.class);
		
		assertEquals("Apples", results.get("favourite.snack"));
	}
	
}
