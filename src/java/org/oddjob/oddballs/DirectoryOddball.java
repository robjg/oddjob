package org.oddjob.oddballs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.deploy.ClassPathDescriptorFactory;
import org.oddjob.arooa.deploy.ClassesOnlyDescriptor;

/**
 * An implementation of an {@link OddballFactory} that creates an
 * {@link Oddball} from a directory.
 * <p>
 * If the given file is not a directory no Oddball is created.
 * 
 * @author rob
 *
 */
public class DirectoryOddball implements OddballFactory {
	private static final Logger logger = Logger.getLogger(DirectoryOddball.class);
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.oddballs.OddballFactory#createFrom(java.io.File, java.lang.ClassLoader)
	 */
	public Oddball createFrom(final File file, ClassLoader parentLoader) {
		
		if (!file.isDirectory()) {
			return null;
		}
		
		URL[] urls = null;
		
		try {
			urls = classpathURLs(file); 
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}  
		
		if (urls.length == 0) {
			return null;
		}
		
		logger.info("Addding Oddall [" + file.getPath() + "]");
		
		final URLClassLoader classLoader = new URLClassLoader(
				urls, parentLoader) {
			@Override
			public String toString() {
				return "Oddball ClassLoader for " + file.getPath();
			}
		};
		
		ClassPathDescriptorFactory descriptorFactory =
			new ClassPathDescriptorFactory();
		descriptorFactory.setExcludeParent(true);
		
		ArooaDescriptor maybeDescriptor =
			descriptorFactory.createDescriptor(classLoader);
		
		if (maybeDescriptor == null) {
			logger.debug("No arooa.xml in Oddball. Using a classes only.");
			maybeDescriptor = new ClassesOnlyDescriptor(classLoader);
		}
		
		final ArooaDescriptor descriptor = maybeDescriptor;
		
		return new Oddball() {
			public ClassLoader getClassLoader() {
				return classLoader;
			}
			public ArooaDescriptor getArooaDescriptor() {
				return descriptor;
			}
		};
	}
	
	URL[] classpathURLs(File parent) throws IOException {
		
		File[] jars = new File(parent, "lib").listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		
		// no lib dir.
		if (jars == null) {
			jars = new File[0];
		}

		int offset;
		URL[] urls;
		
		File classesDir = new File(parent, "classes");
		if (classesDir.exists()) {
			urls = new URL[jars.length + 1];
			urls[0] = classesDir.getCanonicalFile().toURI().toURL();
			offset = 1;
		}
		else {
			urls = new URL[jars.length];
			offset = 0;
		}
		
		for (int i = 0; i < jars.length; ++i) {
			urls[i+offset] = jars[i].getCanonicalFile().toURI().toURL();
		}
		
		return urls;
	}
}
