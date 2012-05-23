/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.launch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * By Default, launch Oddjob using a classloader with the following:
 * <ul>
 *   <li>Any jars in the lib directory.</li>
 *   <li>Any jars in the opt/lib directory.</li>
 *   <li>The opt/classes directory.</li>
 * </ul>
 * 
 * The launcher can also be used as a job to launch other main methods.
 * 
 * @author Rob Gordon, Based on Ant.
 */
public class Launcher implements Runnable {

	public static final String ODDJOB_HOME_PROPERTY = "oddjob.home";
	
	public static final String ODDJOB_RUN_JAR_PROPERTY = "oddjob.run.jar";
	
    public static final String ODDJOB_MAIN_CLASS = "org.oddjob.Main";

    /** The class loader to find the main class in. */
	private ClassLoader classLoader;

	/** The name of the class that contains the main method. */
	private String className;
	
	/** The arguments to pass to main. */
	private String[] args;
	
    public void run() {
    	
    	if (classLoader == null) {
    		throw new NullPointerException("No ClassLoader.");
    	}
    	if (className == null) {
    		throw new NullPointerException("No Class Name.");
    	}
    	
    	ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Class<?> mainClass = classLoader.loadClass(className);
            
            // use reflection because main is now in another
            // class loader space so we can't get at it.
            Method method = mainClass.getMethod("main", 
            	        new Class[] { String[].class });
            
            method.invoke(null, (Object) args);
            
        } catch (RuntimeException e) {
        	throw e;
        } catch (Exception e) {
        	throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(currentLoader);        	
        }
    }

    /**
     * Provides the Oddjob class loader.
     * 
     * @param currentLoader
     * @param classpath
     * @return
     * @throws IOException
     */
	static ClassLoader getClassLoader(ClassLoader currentLoader, String[] classpath) 
	throws IOException {
		
        File sourceJar = Locator.getClassSource(Launcher.class);
        File jarDir = sourceJar.getParentFile();        
        
        System.setProperty(ODDJOB_HOME_PROPERTY, jarDir.getCanonicalPath());
        System.setProperty(ODDJOB_RUN_JAR_PROPERTY, sourceJar.getCanonicalPath());
                
        List<File> classPathList = new ArrayList<File>();
        
        // add the source jar
        classPathList.add(sourceJar);
        
        // expand the classpath entries.
        for (String entry : classpath) {
        	File[] entryFiles = new FileSpec(
            		new File(entry)).getFiles();
        	classPathList.addAll(Arrays.asList(entryFiles));
        }
            
        // expand the lib directory
    	File[] libFiles = new FileSpec(
    		new File(new File(jarDir,"lib"), "*.jar")).getFiles();
    	classPathList.addAll(Arrays.asList(libFiles));
    	
    	// add opt/classes
    	classPathList.add(new File(jarDir, "opt/classes"));
    	
    	// expand the opt/lib directory
        File[] optFiles = new FileSpec(new File(
        		new File(jarDir,"opt/lib"), "*.jar")).getFiles();
    	classPathList.addAll(Arrays.asList(optFiles));
        
    	// The full class path
    	ClassPathHelper classPathHelper = new ClassPathHelper(
    			classPathList.toArray(new File[classPathList.size()]));
        
        URL[] urls = classPathHelper.toURLs();
        classPathHelper.appendToJavaClassPath();
        final String classPath = classPathHelper.toString();
        
        ClassLoader cl = new URLClassLoader(urls, currentLoader) {
        	@Override
        	public String toString() {
        		return "Oddjob Launcher ClassLoader: " + 
        					classPath;
        	}
        };
        return cl;
	}	

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
		
	/**
	 * Main method for launching Oddjob in it's own class loader.
	 * The parent class loader will be taken to be the current threads
	 * context class loader.
	 * 
	 * @param args
	 * @throws IOException
	 */
    public static void main(String... args) throws IOException {

    	// process -D property definitions
    	args = new SystemPropertyArgParser().processArgs(args);
    	
    	// process class path
    	PathParser path = new PathParser();
    	args = path.processArgs(args);
    	
        ClassLoader loader = getClassLoader(
        		Thread.currentThread().getContextClassLoader(), 
        		path.getElements());

    	Launcher launcher = new Launcher();
    	
    	launcher.setArgs(args);
        launcher.setClassLoader(loader);
        launcher.setClassName(ODDJOB_MAIN_CLASS);
    	launcher.run();
    }
    
}
