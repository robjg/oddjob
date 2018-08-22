package org.oddjob.jobs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.oddjob.launch.Launcher;

/**
 * @oddjob.description Launch an application via it's main method. The 
 * application is launched in same JVM as Oddjob, but in it's own class loader.
 * 
 * @oddjob.example
 * 
 * An Oddjob the launches Oddjob. args[0] is org.oddjob.Main, args[1] is the 
 * oddjob home directory. The classes directory is included in the class path
 * for the log4j.properties file otherwise Log4j would attempt to use one
 * from ClassLoader.getSystemLoader() which will be the original application
 * class loader.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/LaunchExample.xml}
 * 
 * 
 * @author rob
 *
 */
public class LaunchJob implements Runnable, Serializable {
	private static final long serialVersionUID = 2010071400L;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The name of this job. 
	 * @oddjob.required No.
	 */
	private String name;
	
	private transient Launcher launcher;
	
	public LaunchJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		launcher = new Launcher();
	}

	
	
	
	@Override
	public void run() {
		launcher.run();
	}
	
	public ClassLoader getClassLoader() {
		return launcher.getClassLoader();
	}

	/** 
	 * @oddjob.property classLoader
	 * @oddjob.description The class loader in which to find the main class.
	 * @oddjob.required Yes.
	 */
	public void setClassLoader(ClassLoader classLoader) {
		launcher.setClassLoader(classLoader);
	}

	public String getClassName() {
		return launcher.getClassName();
	}

	/** 
	 * @oddjob.property className
	 * @oddjob.description The name of the class that contains the main method.
	 * @oddjob.required Yes.
	 */
	public void setClassName(String className) {
		launcher.setClassName(className);
	}

	public String[] getArgs() {
		return launcher.getArgs();
	}

	/** 
	 * @oddjob.property args
	 * @oddjob.description The arguments to pass to main.
	 * @oddjob.required No.
	 */
	public void setArgs(String[] args) {
		launcher.setArgs(args);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}	
}
