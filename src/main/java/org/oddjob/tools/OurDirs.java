package org.oddjob.tools;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to work out relative directories, when running tests individually
 * from eclipse or from ant.
 * <p>
 * When running from ant the property basedir should be set which is the
 * project root.
 * 
 * @author rob
 */
public class OurDirs {
	private static final Logger logger = LoggerFactory.getLogger(OurDirs.class);

	private final File base;
	
	public OurDirs() {
		this("basedir");
		File build = new File(base, "build.xml");
		if (!build.exists()) {
			throw new IllegalStateException("Can't find " +
					build + ", where you running this from?");
		}
	}
	
	/**
	 * Constructor which builds the base directory from the
	 * given property name.
	 * 
	 * @param property The name of the property that gives
	 * the base directory.
	 */
	public OurDirs(String property) {
		String baseDir = System.getProperty(property);
		if (baseDir != null) {
			base = new File(baseDir);
		}
		else {
			base = new File(".");
		}
		logger.info("base is " + base.getAbsolutePath());
	}
	
	public File base() {
		try {
			return base.getCanonicalFile();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public File relative(String name) {
		try {
			return new File(base, name).getCanonicalFile();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
