package org.oddjob;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.reflect.ArooaNoPropertyException;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.types.XMLConfigurationType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.oddballs.BuildOddballs;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OurDirs;

public class OddjobModulesTest extends TestCase {
	private static final Logger logger = Logger.getLogger(OddjobModulesTest.class);
		
	@Override
	protected void setUp() throws Exception {
	
		logger.debug("-------------------  " + getName() + "  -------------");
		
		new BuildOddballs().run();
	}
	
	/**
	 * Show working with a component in a different class loader.
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws ArooaNoPropertyException 
	 */
	public void testClassLoaderAssumptions() throws MalformedURLException, ClassNotFoundException, ArooaNoPropertyException {

		OurDirs dirs = new OurDirs();
		
		File file = new File(dirs.base(), "test/oddballs/apple/classes/");
		
		URL[] urls = { file.toURI().toURL() };
		
		URLClassLoader test = new URLClassLoader(urls);
				
		BeanUtilsPropertyAccessor propertyAccessor = 
			new BeanUtilsPropertyAccessor();
		
		Class<?> appleClass = Class.forName("fruit.Apple", true, test);
		
		BeanOverview overview = new SimpleArooaClass(
				appleClass).getBeanOverview(
					propertyAccessor);
		
		Class<?> colourType = overview.getPropertyType("colour");
		
		assertEquals("fruit.Colour", colourType.getName());
	}
	
	/**
	 * Show resources in a different class loader.
	 * @throws IOException 
	 */
	public void testClassLoaderAssumptions2() throws IOException {

		OurDirs dirs = new OurDirs();
		
		File file = new File(dirs.base(), "test/oddballs/apple/classes/");
		
		URL[] urls = { file.toURI().toURL() };
		
		URLClassLoader test = new URLClassLoader(urls);
		
		Enumeration<URL> allResourses = test.getResources("META-INF/arooa.xml");
		Enumeration<URL> parentResourses = test.getParent().getResources("META-INF/arooa.xml");

		Set<URL> results = toSet(allResourses);
		results.removeAll(toSet(parentResourses));
				
		assertEquals(1, results.size());
		assertTrue(results.contains(new File(dirs.base(), 
				"test/oddballs/apple/classes/META-INF/arooa.xml").getAbsoluteFile().toURI().toURL()));
		
		test.close();
	}

	private <T> Set<T> toSet(Enumeration<T> enumeration) {
		Set<T> set = new HashSet<T>();
		while (enumeration.hasMoreElements()) {
			T next = enumeration.nextElement();
			set.add(next);
		}
		
		return set;
	}
	
	public void testOneModule() {
		
		OurDirs dirs = new OurDirs();
		
		String inner = 
        	"       <oddjob xmlns:fruit='http://rgordon.co.uk/fruit'>" +
        	"        <job>" +
        	"         <fruit:apple id='apple'>" +
        	"          <colour>" +
        	"           <fruit:colour colour='RED' shiny='true'/>" +
        	"          </colour>" +
        	"         </fruit:apple>" +
        	"        </job>" +
        	"       </oddjob>";
			
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <oddjob id='inner1'>" +
			"     <classLoader>" +
            "      <url-class-loader>" +
            "       <files>" +
            "        <files files='" + dirs.base() + "/test/oddballs/apple/classes/'/>" +
            "       </files>" +
            "      </url-class-loader>" +
			"     </classLoader>" +
            "     <descriptorFactory>" +
        	"      <import file='" + dirs.base() + "/test/oddballs/apple/classes/META-INF/arooa.xml'/>" +
        	"     </descriptorFactory>" +
        	"     <configuration>" +
        	"      <value value='${inner-config}'/>" +
        	"     </configuration>" +
        	"    </oddjob>" +
        	"    <echo>Colour As String: ${inner1/apple.colour}</echo>" +
        	"   </jobs>" + 
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		XMLConfigurationType configType = new XMLConfigurationType();
		configType.setXml(inner);
		
		oddjob.setExport("inner-config", configType);
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
	}
}
