package org.oddjob.oddballs;

import java.io.File;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;

public class OddballsDirDescriptorFactory implements ArooaDescriptorFactory {
	
	private static final Logger logger = Logger.getLogger(OddballsDirDescriptorFactory.class);
	
	private File baseDir;

	private OddballFactory oddballFactory;
	
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

	public OddballFactory getOddballFactory() {
		return oddballFactory;
	}

	public void setOddballFactory(OddballFactory oddballFactory) {
		this.oddballFactory = oddballFactory;
	}

	public ArooaDescriptor createDescriptor(ClassLoader classLoader) {
		
		if (baseDir == null) {
			throw new NullPointerException("Base Directory (i.e. oddballs) must be specified.");
		}
		
		logger.info("Scanning directory [" + baseDir.getPath() + "] for Oddballs.");
		
		File[] entries = baseDir.listFiles();
		
		if (entries == null) {
			
			logger.info("No Oddballs found.");
			
			return null;
		}
		
		return new OddballsDescriptorFactory(
				entries, oddballFactory).createDescriptor(classLoader);		
	}

}
