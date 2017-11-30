package org.oddjob;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.ListDescriptorBean;
import org.oddjob.input.ConsoleInputHandler;
import org.oddjob.input.StdInInputHandler;
import org.oddjob.oddballs.OddballsDescriptorFactory;
import org.oddjob.oddballs.OddballsDirDescriptorFactory;

/**
 * Builds an {@link Oddjob} from properties. Used by {@link Main} and 
 * Oddjob's Ant task.
 * 
 * @author rob
 *
 */
public class OddjobBuilder {

	private static final Logger logger = LoggerFactory.getLogger(
			OddjobBuilder.class);
	
	public static final String ODDBALLS_DIR = "oddballs";
	
	private String oddjobHome;
	
	private String oddjobFile;
	
	private String name;
	
	private boolean noBalls;
	
	private File oddballsDir;
	
	private String oddballsPath;

	/**
	 * Build an Oddjob.
	 * 
	 * @return An Oddjob.
	 * 
	 * @throws FileNotFoundException
	 */
	public Oddjob buildOddjob() throws FileNotFoundException {
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setName(name);
		
		String oddjobHome = this.oddjobHome;
		if (oddjobHome == null) {
			oddjobHome =  findOddjobHome();
			if (oddjobHome == null) {
				logger.info("Oddjob Home is not set and can not be derived.");
			}
			else {
				logger.info("Oddjob Home has been derived and is [" + oddjobHome + "]");
			}
		}
		else {
			logger.info("Oddjob Home has been provided and is [" + oddjobHome + "]");
		}
		
		oddjob.setFile(findFileToUse(oddjobFile, oddjobHome));
		
		oddjob.setName(name);
		
		oddjob.setDescriptorFactory(resolveOddballs());
		
		if (System.console() == null) {
			oddjob.setInputHandler(new StdInInputHandler());
		}
		else {
			oddjob.setInputHandler(new ConsoleInputHandler());
		}
		
		return oddjob;
	}

	/**
	 * Find Oddjob Home based on this class location.
	 * 
	 * @return
	 */
	protected String findOddjobHome() {
		
		URL url = getClass().getResource(
				OddjobBuilder.class.getSimpleName() + ".class");
		
		String jar = url.getFile();
		
		File lib = new File(jar).getParentFile();
		
		return lib.getParent();
	}
	
	/**
	 * Work out which file to use
	 * 
	 * @param oddjobFile
	 * 
	 * @return
	 * 
	 * @throws FileNotFoundException
	 */
	public File findFileToUse(String oddjobFile, String oddjobHome) 
	throws FileNotFoundException {
		
		File theFile;
		
		if (oddjobFile == null) {
			theFile = new File("oddjob.xml");
			if (!theFile.exists() && oddjobHome != null) {
				theFile = new File(oddjobHome, "oddjob.xml");
			}
			if (!theFile.exists()) {
				throw new FileNotFoundException(
						"oddjob.xml or ${oddjob.home}/oddjob.xml");
			}
		}
		else {
			theFile = new File(oddjobFile);
			if (!theFile.exists()) {
				throw new FileNotFoundException(oddjobFile);
			}
		}
		return theFile;
	}
	
    protected ArooaDescriptorFactory resolveOddballs() {
    	
    	List<ArooaDescriptorFactory> descriptors = 
    			new ArrayList<ArooaDescriptorFactory>();
		
		if (oddballsPath != null) {
			
			logger.info("Adding Descriptor Factory for path [" + 
					oddballsPath + "]");
			
			descriptors.add(
					new OddballsDescriptorFactory(
							new FileConvertlets().pathToFiles(
									oddballsPath)));
		}
		
		if (oddballsDir != null) {
			
			logger.info("Adding Descriptor Factory for Oddballs dir [" + 
					oddballsDir + "]");

			descriptors.add(
					new OddballsDirDescriptorFactory(oddballsDir));			
		}
		
		if (!noBalls) {
			File defaultOddballsDir = new File(oddjobHome, ODDBALLS_DIR);
			
			logger.info("Adding Descriptor factory for default Oddballs from dir [" + 
					defaultOddballsDir + "]");
			
			descriptors.add(
					new OddballsDirDescriptorFactory(defaultOddballsDir));			
		}
		    	
    	switch (descriptors.size()) {
    	case 0:
    		return null;
    	case 1:
    		return descriptors.get(0);
    	default:
    		return new ListDescriptorBean(descriptors);
    	}    	
    }
    
    
	public String getOddjobFile() {
		return oddjobFile;
	}


	public void setOddjobFile(String oddjobFile) {
		this.oddjobFile = oddjobFile;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public File getOddballsDir() {
		return oddballsDir;
	}


	public void setOddballsDir(File oddballsDir) {
		this.oddballsDir = oddballsDir;
	}


	public String getOddballsPath() {
		return oddballsPath;
	}


	public void setOddballsPath(String oddballsPath) {
		this.oddballsPath = oddballsPath;
	}


	public String getOddjobHome() {
		return oddjobHome;
	}


	public void setOddjobHome(String oddjobHome) {
		this.oddjobHome = oddjobHome;
	}

	public boolean isNoBalls() {
		return noBalls;
	}

	public void setNoOddballs(boolean noBalls) {
		this.noBalls = noBalls;
	}

}
