package org.oddjob.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.FilesType;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.tools.CompileJob;

public class URLClassLoaderTypeTest extends TestCase {

	private static final Logger logger = 
		Logger.getLogger(URLClassLoaderTypeTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.debug("-----------------  " + getName() + "  -----------------");

		logger.debug("ClassLoaders: System=" + 
				ClassLoader.getSystemClassLoader() + ", Context=" +
				Thread.currentThread().getContextClassLoader());
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl instanceof URLClassLoader) {
			URL[] urls = ((URLClassLoader) cl).getURLs();
			logger.debug("URLS: " + Arrays.toString(urls));
		}
	}
	
	public void testLoadMixedJob() throws Exception {
		
		ClassLoader existingContextClassLoader = 
				Thread.currentThread().getContextClassLoader();
		
		OurDirs dirs = new OurDirs();
		
		File check = dirs.relative("test/classloader/AJob.class");
		if (!check.exists()) {
			compileSample(dirs);
		}
		
		URLClassLoaderType test = new URLClassLoaderType();
		test.setFiles(new File[] { dirs.relative("test/classloader") } );
		test.setParent(getClass().getClassLoader());
		
		assertEquals("URLClassLoaderType: [" + 
				dirs.relative("test/classloader").toString() + "]",
				test.toString());
		
		ClassLoader classLoader = test.toValue();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setClassLoader(classLoader);
	
		String xml = 
			"<oddjob>" +
			"<job>" +
			"<bean id='test' class='AJob'/>" +
			"</job>" +
			"</oddjob>";
		
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		OddjobLookup lookup = 
			new OddjobLookup(oddjob);
			
		ClassLoader classLoaderWhenRunning = lookup.lookup(
				"test.classLoader", ClassLoader.class);
		
		ClassLoader jobClassLoader = lookup.lookup(
				"test.class.classLoader", ClassLoader.class);
		
		assertEquals(classLoader, jobClassLoader);
		
		assertEquals(existingContextClassLoader,
				classLoaderWhenRunning);
		
		assertEquals(existingContextClassLoader,
				Thread.currentThread().getContextClassLoader());
	}
	
	void compileSample(final OurDirs dirs) {

		File dir = dirs.relative("test/classloader");
		if (new File(dir, "AJob.class").exists()) {
			return;
		}
		
		FilesType sources = new FilesType();
		sources.setFiles(dirs.relative(
				"test/classloader").getPath() +
				File.separator + "*.java"); 

		CompileJob compile = new CompileJob();		
		compile.setFiles(sources.toFiles());
		
		compile.run();
		
		if (compile.getResult() != 0) {
			throw new RuntimeException(
					"Compile failed. See standard output for details.");
		}
	}

	public void testInOddjob() throws URISyntaxException {
	
		OurDirs dirs = new OurDirs();
		
		compileSample(dirs);
		
    	URL url = getClass().getClassLoader().getResource("org/oddjob/util/URLClassLoader.xml");
    	
    	File file = new File(url.toURI());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setArgs(new String[] { dirs.base().getAbsolutePath() });
		oddjob.setFile(file);
		
		oddjob.run();
		
		Object aJob = new OddjobLookup(oddjob).lookup("nested/x");
		
		assertEquals("AJob", aJob.getClass().getName());
	}
	
	public void testNoParent() {
		
		URLClassLoaderType test = new URLClassLoaderType();
		
		test.setFiles(new File[] { });
		test.setNoInherit(true);
		
		ClassLoader loader = test.toValue();
		
		assertNull(loader.getParent());
		
	}
	
	class LogCatcher implements LogListener {
		
		List<String> lines = new ArrayList<String>();
		
		public void logEvent(LogEvent logEvent) {
			lines.add(logEvent.getMessage());
		}
	}
	
	static String EOL = System.getProperty("line.separator");
	
	public void testFromLaunchJar() throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		
		OurDirs dirs = new OurDirs();
		
		File check = dirs.relative("test/classloader/AJob.class");
		if (!check.exists()) {
			compileSample(dirs);
		}
		
		
		File oddjobFile = dirs.relative("test/classloader/classloader-test.xml");
		
		URLClassLoaderType first = new URLClassLoaderType();
		first.setFiles(new File[] { dirs.relative("run-oddjob.jar")});
		first.setNoInherit(true);
		
		ClassLoader loader = first.toValue();
		
		Class<?> launcher = loader.loadClass("org.oddjob.launch.Launcher");
		
		Method m = launcher.getMethod("main", String[].class);
		
		String[] args = new String[] { "-f", oddjobFile.getCanonicalPath() };
				
		LogCatcher log = new LogCatcher();
		
		Oddjob.CONSOLE.addListener(log, LogLevel.INFO, -1, 0);
		
		m.invoke(null, (Object) args);
		
		Oddjob.CONSOLE.removeListener(log);		
		
		System.out.println("*****************************");
		for (String line: log.lines) {
			System.out.print(line);
		}
		System.out.println("*****************************");
		
		assertEquals("Worked." + EOL, log.lines.get(1));
		
	}
	
}
