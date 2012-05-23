package org.oddjob.launch;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Simple helper class for building the class path.
 * 
 * @author rob
 *
 */
class ClassPathHelper {

	static final String CLASS_PATH_PROPERTY = "java.class.path";
	
	private final File[] files;
	
	public ClassPathHelper(File[] files) {
		this.files = files;
	}
	
	public URL[] toURLs() {
		
		URL[] urls = new URL[files.length];
		
		for (int i = 0; i < urls.length; ++i) {
			try {
				urls[i] = files[i].toURI().toURL();
			}
			catch (IOException e) {
	        	throw new RuntimeException("Classpath " + files[i] + 
	        			" is invalid", e);
			}
		}
		return urls;		
	}
	
	public void appendToJavaClassPath() {
		
		StringBuilder builder = new StringBuilder();
		builder.append(System.getProperty(CLASS_PATH_PROPERTY));
		
		// note we start from 1 because the launch jar will
		// already be in the class path.

		for (int i = 1; i < files.length; ++i) {
			if (builder.length() > 0) {
				builder.append(File.pathSeparator);
			}
			try {
				builder.append(files[i].getCanonicalPath());
			}
			catch (IOException e) {
	        	throw new RuntimeException("Classpath " + files[i] + 
	        			" is invalid", e);
			}
		}
		
		System.setProperty(CLASS_PATH_PROPERTY, builder.toString());		
	}
	
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < files.length; ++i) {
			if (builder.length() > 0) {
				builder.append(File.pathSeparator);
			}
			
			builder.append(files[i]);
		}
		
		return builder.toString();		
	}
	
}
