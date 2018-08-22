package org.oddjob.oddballs;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.deploy.ArooaDescriptorBean;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.ListDescriptor;

/**
 * @oddjob.description Create Oddjob job definition descriptors from any 
 * number of directories that follow the Oddball format. 
 * <p>
 * The Oddball directory structure is:
 * 
 * <code><pre>
 * myball
 *   classes
 *     com
 *       acme
 *         MyStuff.class
 *   lib
 *     someutil.jar
 * </pre></code>
 * 
 * You can have either a <code>lib</code> or <code>classes</code> or both 
 * but you must have something.
 * <p>
 * Additionally there can be as many <code>META-INF/arooa.xml</code> resources
 * on that confirm to the {@link ArooaDescriptorBean} format for defining
 * element mappings and conversions.
 * 
 * @oddjob.example
 * 
 * Loading two Oddballs.
 * 
 * {@oddjob.xml.resource org/oddjob/oddballs/OddballsExample.xml}
 * 
 * This is equivalent to launching Oddjob with the oddball path option
 * set as in:
 * 
 * <code><pre>
 * java -jar run-oddjob.jar \
 *      -op test/oddballs/apple:test/oddballs/orange \
 *      -f test/launch/oddballs-launch.xml 
 * </pre></code>
 * 
 * Or if the <code>test/oddballs</code> directory only contains these two
 * directories, then using the oddball directory option:
 * 
 * <code><pre>
 * java -jar run-oddjob.jar \
 *      -ob test/oddballs \
 *      -f test/launch/oddballs-launch.xml 
 * </pre></code>
 * 
 * If the <code>apple</code> and <code>orange</code> directories were
 * copied to Oddjob's Oddballs directory they would be loaded by default.
 * 
 * @author rob
 *
 */
public class OddballsDescriptorFactory implements ArooaDescriptorFactory {

	private static final Logger logger = LoggerFactory.getLogger(OddballsDescriptorFactory.class);
	
	/**
	 * @oddjob.property
	 * @oddjob.description The Oddball directory or directories.
	 * @oddjob.required Yes.
	 */
	private File[] files;

	/**
	 * @oddjob.property
	 * @oddjob.description The factory that will create the Oddball from the
	 * file specification. At the moment this defaults to the only 
	 * implementation which is to load an Oddball from a directory. In future
	 * it is hoped to support loading Oddballs from archives. Following the
	 * existing java naming convention for archives they will probably be
	 * called .oar files.
	 * 
	 * @oddjob.required No. Defaults to a directory loading factory.
	 */
	private OddballFactory oddballFactory;
	
	public OddballsDescriptorFactory() {
		this(null, null);
	}
	
	public OddballsDescriptorFactory(File[] files) {
		this(files, null);
	}
	
	public OddballsDescriptorFactory(File[] files, 
			OddballFactory oddballFactory) {
		this.files = files;
		this.oddballFactory = oddballFactory;
	}
	
	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] baseDir) {
		this.files = baseDir;
	}

	public OddballFactory getOddballFactory() {
		return oddballFactory;
	}

	public void setOddballFactory(OddballFactory oddballFactory) {
		this.oddballFactory = oddballFactory;
	}

	public ArooaDescriptor createDescriptor(ClassLoader classLoader) {

		if (files == null) {
			throw new NullPointerException("No Oddball directories given.");
		}
		
		OddballFactory oddballFactory = this.oddballFactory;
		if (oddballFactory == null) {
			oddballFactory = new DirectoryOddball();
		}
		
		ListDescriptor descriptor = new ListDescriptor();
		
		for (File file : files) {
			
			Oddball oddball = oddballFactory.createFrom(file, classLoader);
			
			if (oddball == null) {
				continue;
			}
			
			descriptor.addDescriptor(oddball.getArooaDescriptor());
		}

		if (descriptor.size() == 0) {
			
			logger.info("No Oddballs found for [" + 
					Arrays.toString(files) + "]");
			
			return null;
		}
		else {
			return descriptor;
		}		
	}
	
	public String toString() {
		return getClass().getName() + ". " + 
			(files == null ? 
					"No Oddball directories!" :
					"Odball directories: " + Arrays.toString(files));
	}
}
