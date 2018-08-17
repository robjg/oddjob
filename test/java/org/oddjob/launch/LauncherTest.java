/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.launch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.Main;
import org.oddjob.io.CopyJob;
import org.oddjob.tools.OurDirs;
import org.oddjob.util.URLClassLoaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Rob Gordon.
 */
public class LauncherTest {

	private static final Logger logger = LoggerFactory.getLogger(LauncherTest.class);

	@Rule public TestName name = new TestName();

	public String getName() {
        return name.getMethodName();
    }

	String oddjobHome;
	
    @Before
    public void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
		
		oddjobHome = System.getProperty("oddjob.home");		
	}
	
    @After
    public void tearDown() throws Exception {
		if (oddjobHome == null) {
			System.getProperties().remove("oddjob.home");
		}
		else {
			System.setProperty("oddjob.home", oddjobHome);
		}
	}
	
   @Test
    public void testGetClassLoader() throws IOException, URISyntaxException {
    	
    	OurDirs dirs = new OurDirs();
    	File f = dirs.relative("src/java");
    	
        ClassLoader cl = Launcher.getClassLoader(
        		LauncherTest.class.getClassLoader(), 
        		new String[] { f.getPath() } );

        assertNotNull("Classloader", cl);

        URL[] urls = ((URLClassLoader)cl).getURLs();
        
        HashSet<String> results = new HashSet<String>();
        
        for (int i = 0; i < urls.length; ++i) {
        	results.add(new File(urls[i].toURI()).getCanonicalPath());
            System.out.println(urls[i]);
        }
        
        assertTrue(results.contains(
        		f.getCanonicalPath()));
        
        assertTrue(System.getProperty(
        		"java.class.path").contains(
        				f.getCanonicalPath()));

        assertEquals(Thread.currentThread().getContextClassLoader(),
        		ClassLoader.getSystemClassLoader());
    }
    
    
   @Test
    public void testInitOddjob() throws Exception {
    	
    	OurDirs dirs = new OurDirs();
    	
    	File result = new File(dirs.base(), "work/launcher.result");
    	result.delete();

    	System.setProperty("oddjob.home", new File(".").getCanonicalPath());
    	
        Launcher test = new Launcher();
        String args[] = { "-nb", "-f", dirs.base() + "/test/conf/launcher-test.xml",
        		dirs.base().toString() };
        
        test.setArgs(args);
        test.setClassLoader(Main.class.getClassLoader());
        test.setClassName(Launcher.ODDJOB_MAIN_CLASS);
        test.run();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        CopyJob copy = new CopyJob();
        copy.setFrom(new File[] { result });
        copy.setOutput(out);
        
        copy.run();
        
        assertEquals("Launcher Worked", out.toString().trim());
        
        assertEquals(new File(".").getCanonicalPath(), 
        		System.getProperty("oddjob.home"));
        
        assertEquals(Thread.currentThread().getContextClassLoader(),
        		ClassLoader.getSystemClassLoader());
    }
    
   @Test
    public void testLaunchAsJob() throws Exception {
    	
    	OurDirs dirs = new OurDirs();
    	
    	File result = new File(dirs.base(), "work/launcher.result");
    	result.delete();

    	System.setProperty("oddjob.home", "Will Be Overritten");
    	
    	File runJar = dirs.relative("run-oddjob.jar");
    	
    	URLClassLoaderType classLoader = new URLClassLoaderType();
    	classLoader.setFiles(new File[] { runJar });
    	classLoader.setNoInherit(true);    	
    	classLoader.configured();
    	
    	ClassLoader context = Thread.currentThread().getContextClassLoader();
    	assertNotNull(context);
    	
    	try {
    		Thread.currentThread().setContextClassLoader(null);
    		
	    	Launcher boot = new Launcher();
	    	boot.setClassLoader(classLoader.toValue());
	    	boot.setClassName(Launcher.class.getName());
	    	boot.setArgs(new String[] {
	    			"-f", 
	    			dirs.relative("test/conf/launcher-boot.xml").getPath() });
	    	boot.run();
    	}
    	finally {
    		Thread.currentThread().setContextClassLoader(context);
    	}
    	
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        CopyJob copy = new CopyJob();
        copy.setFrom(new File[] { result });
        copy.setOutput(out);
        
        copy.run();
        
        assertEquals("Launcher Worked", out.toString().trim());
        
        assertEquals(runJar.getParentFile().getCanonicalPath(), 
        		System.getProperty("oddjob.home"));
        
        assertEquals(Thread.currentThread().getContextClassLoader(),
        		ClassLoader.getSystemClassLoader());
    }
    
}
