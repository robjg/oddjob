package org.oddjob.util;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.oddjob.Structural;
import org.oddjob.oddballs.BuildOddballs;
import org.oddjob.tools.OurDirs;

public class ClassLoaderSorterTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ClassLoaderSorterTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("----------------------  " + getName() + "  -------------------------");
	}
	
	public void testUnderstandingOfCreatingProxyWithWrongClassLoader() throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		logger.info("System class loader is: " + ClassLoader.getSystemClassLoader());
		
		new BuildOddballs().run();
		
		OurDirs dirs = new OurDirs();
		
		URLClassLoader specialLoader =  new URLClassLoader(
				new URL[] { new File(dirs.base() + 
						"/test/oddballs/apple/classes").toURI().toURL() },
				getClass().getClassLoader());
		
		Class<?> fruitClass = Class.forName("fruit.Fruit", true, specialLoader );
				
		Class<?>[] interfaces = { fruitClass, Structural.class };
		
		try { Proxy.newProxyInstance(Structural.class.getClassLoader(), 
				interfaces, 
				Mockito.mock(InvocationHandler.class));
		}
		catch (IllegalArgumentException e) {
			assertEquals("interface fruit.Fruit is not visible from class loader",
					e.getMessage());
		}
		
		Object proxy = Proxy.newProxyInstance(specialLoader, 
				interfaces, 
				Mockito.mock(InvocationHandler.class));
		
		assertTrue(fruitClass.isInstance(proxy));
		
	}
	
	public void testWhenTwoClassesUsedThenHighestClassLoaderIsUsed() throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		new BuildOddballs().run();
		
		OurDirs dirs = new OurDirs();
		
		URLClassLoader specialLoader =  new URLClassLoader(
				new URL[] { new File(dirs.base() + 
						"/test/oddballs/apple/classes").toURI().toURL() },
				getClass().getClassLoader());
		
		Class<?> fruitClass = Class.forName("fruit.Fruit", true, specialLoader );
				
		Class<?>[] interfaces = { List.class, fruitClass, Structural.class };
		
		ClassLoader test = new ClassLoaderSorter().getTopLoader(
				interfaces);
		
		Class<?> result = Class.forName("fruit.Fruit", true, test);
		
		assertEquals(specialLoader, result.getClassLoader());
		
		Object proxy = Proxy.newProxyInstance(test, 
				interfaces, 
				Mockito.mock(InvocationHandler.class));
		
		assertTrue(fruitClass.isInstance(proxy));
	}
	
	public void testWhenSingleStructuralClassProxyRequiredCorrectClassLoaderIsGiven() throws MalformedURLException, ClassNotFoundException {
		
		
		ClassLoader test = new ClassLoaderSorter().getTopLoader(
				new Class<?>[] { Structural.class });
		
		Object proxy = Proxy.newProxyInstance(test, 
				new Class<?>[] { Structural.class }, 
				Mockito.mock(InvocationHandler.class));
		
		assertTrue(Structural.class.isInstance(proxy));
	}
}
