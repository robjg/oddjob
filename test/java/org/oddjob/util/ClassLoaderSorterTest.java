package org.oddjob.util;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.oddjob.OurDirs;
import org.oddjob.Structural;
import org.oddjob.oddballs.BuildOddballs;

public class ClassLoaderSorterTest extends TestCase {

	class OurInvocationHandler implements InvocationHandler {
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return new RuntimeException("Unexpected.");
		}
	}
	
	public void testLoad() throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		new BuildOddballs().run();
		
		OurDirs dirs = new OurDirs();
		
		URLClassLoader specialLoader =  new URLClassLoader(
				new URL[] { new File(dirs.base() + 
						"/test/oddballs/apple/classes").toURI().toURL() } );
		
		Class<?> fruitClass = Class.forName("fruit.Fruit", true, specialLoader );
				
		ClassLoader test = new ClassLoaderSorter().getTopLoader(
				new Class<?>[] { String.class, fruitClass, Structural.class });
		
		Class<?> result = Class.forName("fruit.Fruit", true, test);
		
		assertEquals(specialLoader, result.getClassLoader());
		
		Object proxy = Proxy.newProxyInstance(test, 
				new Class<?>[] { fruitClass, Structural.class }, 
				new OurInvocationHandler());
		
		assertTrue(fruitClass.isInstance(proxy));
	}
	
	public void testStructural() throws MalformedURLException, ClassNotFoundException {
		
		
		ClassLoader test = new ClassLoaderSorter().getTopLoader(
				new Class<?>[] { Structural.class });
		
		Object proxy = Proxy.newProxyInstance(test, 
				new Class<?>[] { Structural.class }, 
				new OurInvocationHandler());
		
		assertTrue(Structural.class.isInstance(proxy));
	}
}
