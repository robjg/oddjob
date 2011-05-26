package org.oddjob.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description A simple wrapper for URLClassloader. 
 * 
 * @oddjob.example
 * 
 * A simple example. A single directory is added to the class path.
 * 
 * {@oddjob.xml.resource org/oddjob/util/URLClassLoader.xml}
 * 
 * @author rob
 *
 */
public class URLClassLoaderType implements ValueFactory<ClassLoader> {

	private static final Logger logger = Logger.getLogger(URLClassLoaderType.class);
	
	private ClassLoader parent;
	
	private URL[] urls;
	
	private File[] files;

	private boolean noInherit;
	
	public ClassLoader toValue() {
		final StringBuilder toString = new StringBuilder();
		
		List<URL> allUrls = new ArrayList<URL>();
		
		if (urls != null) {
			logger.debug("Creating Classloader for URLs:");
			for (URL url: urls) {
				logger.debug(" " + url);
				toString.append((toString.length() == 0 ? 
						"" : ";") + url);
				allUrls.add(url);
			}
		}
		
		if (files != null) {
			logger.debug("Creating Classloader for Files:");
			for (File file: files) {
				logger.debug(" " + file);
				toString.append((toString.length() == 0 ? 
						"" : ";") + file);
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
				allUrls.toArray(new URL[allUrls.size()]), 
				parentLoader) {
			public String toString() {
				return "URLClassLoader: " + toString.toString();
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
	public String toString() {
		return getClass().getSimpleName() + ": " + 
			(files == null ? "" : Arrays.toString(files)) +
			(urls == null ? "" : Arrays.toString(urls));
	}
}
