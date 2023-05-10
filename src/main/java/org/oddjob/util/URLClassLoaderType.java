package org.oddjob.util;

import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.values.VariablesJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @oddjob.description A simple wrapper for URLClassloader. 
 * <p>
 * The class loader is created when this type is configured, and the same
 * class loader is then shared with all jobs that reference this type.
 * Because creating numerous class loader can use up the permgen heap space
 * it is best to avoid creating the type in a loop. Instead, add it to
 * {@link VariablesJob} outside the loop and only reference it inside the
 * loop.
 * <p>
 * The parent class loader will be set to Oddjob's classloader by default. To avoid this use the {@code noInherit}
 * property.
 * </p>
 *
 * @oddjob.example
 * 
 * A simple example. A single directory is added to the class path.
 * 
 * {@oddjob.xml.resource org/oddjob/util/URLClassLoader.xml}
 *
 * @oddjob.example
 *
 * Forcing the platform class loader as a parent. When migrating to Java 9 classes such as {@code java.sql.date}
 * will no longer load with the no parent class loader. See
 * <a href="https://docs.oracle.com/javase/9/migrate/toc.htm#JSMIG-GUID-D867DCCC-CEB5-4AFA-9D11-9C62B7A3FAB1">
 *     Migrating to JDK 9</a> for more information.
 *
 * @author rob
 *
 */
public class URLClassLoaderType 
implements ValueFactory<ClassLoader>, ArooaLifeAware {

	private static final Logger logger = LoggerFactory.getLogger(URLClassLoaderType.class);
	
	private ClassLoader parent;
	
	private ClassLoader classLoader;
	
	private URL[] urls;
	
	private File[] files;

	private boolean noInherit;
	
	public ClassLoader toValue() {
		if (classLoader == null) {
			throw new IllegalStateException("Not Configured!");
		}
		return classLoader;
	}
	
	protected ClassLoader createClassLoader() {
		
		final StringBuilder toString = new StringBuilder();
		
		List<URL> allUrls = new ArrayList<>();
		
		if (urls != null) {
			logger.debug("Creating Classloader for URLs:");
			for (URL url: urls) {
				logger.debug(" " + url);
				toString.append(toString.length() == 0 ?
						"" : ";")
						.append(url);
				allUrls.add(url);
			}
		}
		
		if (files != null) {
			logger.debug("Creating Classloader for Files:");
			for (File file: files) {
				logger.debug(" " + file);
				toString.append(toString.length() == 0 ?
						"" : ";")
						.append(file);
				try {
					allUrls.add(file.toURI().toURL());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}				
			}
		}		
	
		ClassLoader parentLoader = parent;
		if (noInherit) {
			parentLoader = null;
		}
		
		return new URLClassLoader(
				allUrls.toArray(new URL[0]),
				parentLoader) {
			public String toString() {
				return "URLClassLoader: " + toString;
			}
		};
	}
	
	public URL[] getUrls() {
		return urls;
	}

    /**
     * @oddjob.property urls
     * @oddjob.description URLs to add to the classpath.
     * @oddjob.required No.
     */
	public void setUrls(URL[] urls) {
		this.urls = urls;
	}

	public File[] getFiles() {
		return files;
	}

    /**
     * @oddjob.property files 
     * @oddjob.description Files to add to the classpath.
     * @oddjob.required No.
     */
	public void setFiles(File[] files) {
		this.files = files;
	}
	
	public boolean isNoInherit() {
		return noInherit;
	}

    /**
     * @oddjob.property noInherit 
     * @oddjob.description Don't inherit the parent class loader.
     * @oddjob.required No.
     * 
     */
	public void setNoInherit(boolean noInherit) {
		this.noInherit = noInherit;
	}

	public ClassLoader getParent() {
		return parent;
	}

    /**
     * @oddjob.property parent
     * @oddjob.description The parent class loader to inherit.
     * @oddjob.required No, defaults to any existing Oddjob 
     * class loader.
     * 
     */
	@Inject
	public void setParent(ClassLoader parent) {
		this.parent = parent;
	}	
	
	@Override
	public void initialised() {
	}
	
	@Override
	public void configured() {
		classLoader = createClassLoader();
	}
	
	@Override
	public void destroy() {
		classLoader = null;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + 
			(files == null ? "" : Arrays.toString(files)) +
			(urls == null ? "" : Arrays.toString(urls));
	}
}
