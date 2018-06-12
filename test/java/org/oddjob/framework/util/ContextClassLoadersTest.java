package org.oddjob.framework.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.tools.OurDirs;
import org.oddjob.util.URLClassLoaderTypeTest;

public class ContextClassLoadersTest extends OjTestCase {

   @Test
	public void testSimple() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		
		OurDirs dirs = new OurDirs();
		
		ClassLoader existing = Thread.currentThread().getContextClassLoader();
		
		File check = dirs.relative("test/classloader/AJob.class");
		if (!check.exists()) {
			URLClassLoaderTypeTest.compileSample(dirs);
		}
		
		URLClassLoader classLoader = new URLClassLoader(new URL[] {
				dirs.relative("test/classloader").toURI().toURL() }, 
				this.getClass().getClassLoader());
		
		Object comp = Class.forName("AJob", true, classLoader).newInstance();
		
		ContextClassloaders.push(comp);
		
		assertEquals(classLoader, 
				Thread.currentThread().getContextClassLoader());
		
		ContextClassloaders.pop();
		
		assertEquals(existing, Thread.currentThread().getContextClassLoader());
	}
}
