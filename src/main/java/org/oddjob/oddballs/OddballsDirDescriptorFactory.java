package org.oddjob.oddballs;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Creates an Arooa Descriptor from a base directory that contains a number of Oddball directories.
 *
 * @see OddballsDescriptorFactory
 */
public class OddballsDirDescriptorFactory implements ArooaDescriptorFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(OddballsDirDescriptorFactory.class);
	
	private File baseDir;

	public OddballsDirDescriptorFactory() {
		
	}
	
	public OddballsDirDescriptorFactory(File baseDir) {
		this.baseDir = baseDir;
	}
	
	
	
	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public ArooaDescriptor createDescriptor(ClassLoader classLoader) {
		
		if (baseDir == null) {
			throw new NullPointerException("Base Directory (i.e. oddballs) must be specified.");
		}

        logger.info("Scanning directory [{}] for Oddballs.", baseDir.getPath());
		
		File[] entries = baseDir.listFiles();
		
		if (entries == null) {
			
			logger.info("No Oddballs found.");
			
			return null;
		}
		
		return new OddballsDescriptorFactory(entries)
				.createDescriptor(classLoader);
	}

}
