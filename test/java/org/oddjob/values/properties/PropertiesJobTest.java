/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.values.properties;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.persist.MapPersister;
import org.oddjob.state.JobState;

/**
 * Test for PropertiesType.
 * 
 * @author rob
 *
 */
public class PropertiesJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(PropertiesJobTest.class);

	@Override
	protected void setUp() throws Exception {
		logger.info("---------------------  " + getName() + "  ------------------");
	}
	
	/**
	 * Test a simple set and a get.
	 * 
	 * @throws Exception
	 */
	public void testSimpleSetGet() throws Exception {

		String xml = 
			"<oddjob>" +
			" <job>" +
			"    <properties id='test'>" +
			"     <values>" +
			"      <value key='snack.favourite' value='apple'/>" +
			"     </values>" +
			"    </properties>" +
			" </job>" +
			"</oddjob>";

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals("apple", lookup.lookup("test.properties(snack.favourite)", String.class));
		
//		assertEquals("apple", lookup.lookup("snack.favourite"));
		
		Resetable properties = lookup.lookup("test", Resetable.class);
		
		properties.hardReset();
		
		assertEquals("apple", lookup.lookup("test.properties(snack.favourite)", String.class));
		
		assertEquals(null, lookup.lookup("snack.favourite"));
		
		oddjob.destroy();
	}
	
	/**
	 */
	public void testPropertiesFromValues() throws Exception {

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/properties/PropertiesJobFromValues.xml",
				getClass().getClassLoader()));
		
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals("${snack.favourite} is apple", lookup.lookup("echo.text", String.class));
		
		oddjob.destroy();
	}
	
	/**
	 * Test setting properties from an input stream. 
	 */
	public void testSetFromInput() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/properties/PropertiesJobFromInput.xml",
				getClass().getClassLoader()));

		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals("john",
				lookup.lookup("echo.text", String.class));
		
		oddjob.destroy();
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

	public void testSetPropertiesFromFile() {
		
		OurDirs dirs = new OurDirs();
		
		String xml = 
			"<oddjob id='oj'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <properties id='props'>" +
			"     <input>" +
			"      <file file='" + dirs.base() + "/test/types/PropertyTypeTest.props'/>" +
			"     </input>" +
			"    </properties>" +
			"    <bean id='mycomp' class='" + MyComp.class.getName() + "'>" +
			"     <props>" +
			"      <value value='${props.properties}'/>" +
			"     </props>" +
			"    </bean>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
				
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();
		
		MyComp myComp = (MyComp) new OddjobLookup(oddjob).lookup("mycomp");

		assertNotNull(myComp);
		assertEquals("test", myComp.props.get("a.b.c"));		
	}
		
	public void testSetFromPrevious() {
		String xml = 
			"<oddjob id='oj'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <properties>" +
			"     <values>" +
			"      <value key='favourite.fruit' value='apple'/>" +
			"      <value key='snack.fruit' value='${favourite.fruit}'/>" +
			"     </values>" +
			"    </properties>" +
			"    <bean id='mycomp' class='" + MyComp.class.getName() + "'>" +
			"     <props>" +
			"      <properties>" +
			"       <values>" +
			"        <value key='snack.fruit' value='${snack.fruit}'/>" +
			"       </values>" +
			"      </properties>" +
			"     </props>" +
			"    </bean>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
				
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();
		
		MyComp myComp = (MyComp) new OddjobLookup(oddjob).lookup("mycomp");

		assertNotNull(myComp);
		assertEquals("apple", myComp.props.get("snack.fruit"));		
	}
		
	
	public void testSettingSelfFromPrevious() throws ArooaConversionException {
		String xml = 
			"<oddjob id='oj'>" +
			" <job>" +
			"  <properties id='test'>" +
			"   <values>" +
            "    <value key='dist.dir' value='ojdist'/>" + 
            "    <value key='dist.name' value='oddjob-0.27.0'/>" +
            "    <value key='dist.dir.src' value='${dist.dir}/src/${dist.name}'/>" +
            "    <value key='dist.dir.bin' value='${dist.dir}/bin/${dist.name}'/>" + 
			"   </values>" +
			"  </properties>" +
			" </job>" +
			"</oddjob>";
				
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		assertEquals("ojdist/src/oddjob-0.27.0", 
				lookup.lookup("test.properties(dist.dir.src)", String.class));
		assertEquals("ojdist/bin/oddjob-0.27.0", 
				lookup.lookup("test.properties(dist.dir.bin)", String.class));

		Properties properties = lookup.lookup("test.properties", Properties.class);
		
		assertEquals(4, properties.size());
		assertEquals("ojdist", properties.getProperty("dist.dir"));
		assertEquals("oddjob-0.27.0", properties.getProperty("dist.name"));
		assertEquals("ojdist/src/oddjob-0.27.0", properties.getProperty("dist.dir.src"));
		assertEquals("ojdist/bin/oddjob-0.27.0", properties.getProperty("dist.dir.bin"));
		
		oddjob.destroy();
	}
		
	public void testMergeFiles() throws ArooaPropertyException, ArooaConversionException {
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <properties substitute='true'>" +
			"     <sets>" +
			"      <properties>" +
			"       <input>" +
			"        <resource resource='org/oddjob/values/properties/PropertiesTypeTest1.properties'/>" +
			"       </input>" +
			"      </properties>" +
			"      <properties>" +
			"       <input>" +
			"        <resource resource='org/oddjob/values/properties/PropertiesTypeTest2.properties'/>" +
			"       </input>" +
			"      </properties>" +
			"     </sets>" +
			"    </properties>" +
			"    <echo id='echo'>${snack.favourite}</echo>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		oj.run();
		
		 String result = new OddjobLookup(oj).lookup("echo.text",
				 String.class);

		assertEquals("apples", result);
		
		oj.destroy();
	}
	
	public void testSerialzation() throws IOException, ClassNotFoundException, ArooaPropertyException, ArooaConversionException {
	
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <properties id='test'>" +
			"     <values>" +
			"      <value key='snack.favourite' value='apple'/>" +
			"     </values>" +
			"    </properties>" +
			"    <variables id='vars'>" +
			"     <result>" +
			"      <value value='${snack.favourite}'/>" +
			"     </result>" +
			"    </variables>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";

		MapPersister persister = new MapPersister();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.setPersister(persister);
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful test1 = lookup.lookup("test", Stateful.class);
		assertEquals(JobState.COMPLETE, 
				test1.lastStateEvent().getState());

		String r1 = lookup.lookup("vars.result", String.class);
		
		assertEquals("apple", r1);

		Oddjob second = new Oddjob();
		
		second.setConfiguration(new XMLConfiguration("XML", xml));
		second.setPersister(persister);
		
		second.load();
		
		lookup = new OddjobLookup(second);
		
		Stateful test2 = lookup.lookup("test", Stateful.class);
		assertEquals(JobState.COMPLETE, 
				test2.lastStateEvent().getState());
		
		String r2 = lookup.lookup("vars.result", String.class);
		
		assertNull("apple", r2);
		
		Runnable vars = lookup.lookup("vars", Runnable.class);
		
		vars.run();
		
		String r3 = lookup.lookup("vars.result", String.class);
		
		assertEquals("apple", r3);
		
		oddjob.destroy();
	}
	
	public void testOverridingProperties() throws ArooaPropertyException, ArooaConversionException {
		
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/properties/PropertiesJobOverriding.xml",
				getClass().getClassLoader()));

		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals("${fruit.favourite} is apple",
				lookup.lookup("echo1.text", String.class));
		
		assertEquals("${fruit.favourite} is banana",
				lookup.lookup("echo2.text", String.class));
		
		oddjob.destroy();
	}
}
