package org.oddjob.oddballs;

import org.oddjob.OddjobException;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.deploy.ArooaDescriptorBean;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.ListDescriptor;
import org.oddjob.arooa.utils.ListSetterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @oddjob.description Create Oddjob job definition descriptors from any 
 * number of directories that follow the Oddball format. 
 * <p>
 * The Oddball directory structure is:
 * <code><pre>
 * myball
 *   classes
 *     com
 *       acme
 *         MyStuff.class
 *   lib
 *     someutil.jar
 * </pre></code>
 * You can have either a <code>lib</code> or <code>classes</code> or both,
 * but you must have something.
 * <p>
 * Additionally there can be as many <code>META-INF/arooa.xml</code> resources
 * on that confirm to the {@link ArooaDescriptorBean} format for defining
 * element mappings and conversions.
 * 
 * @oddjob.example
 * 
 * Loading two Oddballs.
 * {@oddjob.xml.resource org/oddjob/oddballs/OddballsFilesExample.xml}
 * 
 * This is equivalent to launching Oddjob with the oddball path option
 * set as in:
 * <code><pre>
 * java -jar run-oddjob.jar \
 *      -op test/oddballs/apple:test/oddballs/orange \
 *      -f test/launch/oddballs-launch.xml 
 * </pre></code>
 * Or if the <code>test/oddballs</code> directory only contains these two
 * directories, then using the oddball directory option:
 * <code><pre>
 * java -jar run-oddjob.jar \
 *      -ob test/oddballs \
 *      -f test/launch/oddballs-launch.xml 
 * </pre></code>
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
	 * @oddjob.description Other factories for creating Oddballs. See {@link OddballFactoryType}.
     * In the future, it is hoped to support loading Oddballs from archives. Following the
	 * existing java naming convention for archives they will probably be
	 * called .oar files.
	 * @oddjob.required No. Defaults to a directory loading factory.
	 */
	private final ListSetterHelper<OddballFactory> oddballs = new ListSetterHelper<>();
	
	public OddballsDescriptorFactory() {
		this(null);
	}
	
	public OddballsDescriptorFactory(File[] files) {
		this.files = files;
	}
	
	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] baseDir) {
		this.files = baseDir;
	}

	public OddballFactory getOddballs(int index) {
		return oddballs.get(index);
	}

	public void setOddballs(int index, OddballFactory oddballFactory) {
		oddballs.set(index, oddballFactory);
	}

	public ArooaDescriptor createDescriptor(ClassLoader classLoader) {

		List<OddballFactory> factories = new ArrayList<>();
		if (files != null) {
			factories.addAll(
					Arrays.stream(files)
							.map(DirectoryOddballFactory::from)
							.collect(Collectors.toList()));
		}

		factories.addAll(oddballs.getList());

		ListDescriptor descriptor = new ListDescriptor();
		
		for (OddballFactory factory: factories) {

            Oddball oddball;
            try {
                oddball = factory.createFrom(classLoader);
            } catch (Exception e) {
                throw new OddjobException(e);
            }

            if (oddball == null) {
				continue;
			}
			
			descriptor.addDescriptor(oddball.getArooaDescriptor());
		}

		if (descriptor.size() == 0) {

            logger.info("No Oddballs found for [{}]",
					factories);
			
			return null;
		}
		else {
			return descriptor;
		}		
	}
	
	public String toString() {
		return getClass().getName() + ". " + 
			(files == null ? 
					"No Oddball directories" :
					"Odball directories: " + Arrays.toString(files))
                + ", " +
            (oddballs.isEmpty() ?
                    "No Other Oddballs" :
                    oddballs.getList());
	}
}
