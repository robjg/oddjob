package org.oddjob.jobs;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;

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
	private static final long serialVersionUID = 2020042800L;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The name of this job. 
	 * @oddjob.required No.
	 */
	private String name;

	/**
	 * @oddjob.property classLoader
	 * @oddjob.description The class loader in which to find the main class.
	 * @oddjob.required Yes.
	 */
	private transient ClassLoader classLoader;

	/**
	 * @oddjob.property className
	 * @oddjob.description The name of the class that contains the main method.
	 * @oddjob.required Yes.
	 */
	private String className;

	/**
	 * @oddjob.property args
	 * @oddjob.description The arguments to pass to main.
	 * @oddjob.required No.
	 */
	private String[] args;

	@Override
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
					String[].class);

			method.invoke(null, (Object) args);

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(currentLoader);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	@Override
	public String toString() {
		return Optional.ofNullable(name)
				.orElseGet(() -> getClass().getSimpleName());
	}
}
