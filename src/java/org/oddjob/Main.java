package org.oddjob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.ListDescriptorBean;
import org.oddjob.input.ConsoleInputHandler;
import org.oddjob.input.StdInInputHandler;
import org.oddjob.oddballs.OddballsDescriptorFactory;
import org.oddjob.oddballs.OddballsDirDescriptorFactory;


/**
 * This is a very simple wrapper around Oddjob with a main method.
 * 
 * @author Rob Gordon
 */
public class Main {
	private static Logger logger;

	private static Logger logger() {
		if (logger == null) {
			logger = Logger.getLogger(Main.class);
		}
		return logger;
	}
	
	public static final String ODDBALLS_DIR = "oddballs";
	
	public static final String USER_PROPERTIES = "oddjob.properties";

	/**
	 * Parse the command args and configure Oddjob.
	 * 
	 * @param args The args.
	 * @return A configured and ready to run Oddjob.
	 * @throws FileNotFoundException 
	 */
    public OddjobRunner init(String args[]) throws IOException {
	    
    	Properties props = processUserProperties();
    	
		String oddjobFile = null;
		String name = null;
		String logConfig = null;
		File oddballsDir = null;
		String oddballsPath = null;
		
		String oddjobHome = System.getProperty("oddjob.home");
		if (oddjobHome != null) {
			oddballsDir = new File(oddjobHome, ODDBALLS_DIR);
		}
		
		int startArg = 0;
		
		// cycle through given args
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.equals("-h") || arg.equals("-help")) {
			    usage();
			    return null;
			} else if (arg.equals("-v") || arg.equals("-version")) {
			    version();
			    return null;
			}
			else if (arg.equals("-n") || arg.equals("-name")) {
				name = args[++i];
				startArg += 2;
			} else if (arg.equals("-l") || arg.equals("-log")) {
			    logConfig = args[++i];
				startArg += 2;
			} else if (arg.equals("-f") || arg.equals("-file")) {
				oddjobFile = args[++i];
				startArg += 2;
			} else if (arg.equals("-nb") || arg.equals("-noballs")) {
				oddballsDir = null;
				startArg += 1;
			} else if (arg.equals("-ob") || arg.equals("-oddballs")) {
				oddballsDir = new File(args[++i]);
				startArg += 2;
			} else if (arg.equals("-op") || arg.equals("-obpath")) {
				oddballsPath = args[++i];
				startArg += 2;
			} else if (arg.equals("--")) {
				startArg += 1;
				break;
			} else {
				// unrecognised arg, so also pass though to oddjob;
				break;
			}
		}

		if (logConfig != null) {
			configureLog(logConfig);
		}
		// crude attempt to see if logging is initialise either statically or via
		// a file. If it hasn't set a default so we don't get warnings.
		Enumeration<?> enumeration = Logger.getRootLogger().getAllAppenders();
		boolean hasAppenders = enumeration.hasMoreElements();
		if (!hasAppenders) {
		    Logger.getRootLogger().addAppender(new ConsoleAppender(
		    		new PatternLayout("%-5p %m%n")));
		    Logger.getRootLogger().setLevel(Level.ERROR);
		}
		
		final Oddjob oddjob = new Oddjob();
		
		oddjob.setFile(findFileToUse(oddjobFile, oddjobHome));
		
		oddjob.setName(name);
		
		oddjob.setDescriptorFactory(
				resolveOddballs(oddballsPath, oddballsDir));
		
		if (System.console() == null) {
			oddjob.setInputHandler(new StdInInputHandler());
		}
		else {
			oddjob.setInputHandler(new ConsoleInputHandler());
		}
		
		oddjob.setProperties(props);
		
		// pass remaining args into Oddjob.
		Object newArray = Array.newInstance(String.class, args.length - startArg);
	    System.arraycopy(args, startArg, newArray, 0, args.length - startArg);
	    oddjob.setArgs((String[]) newArray);
	    	    
		return new OddjobRunner(oddjob);
	}

    protected ArooaDescriptorFactory resolveOddballs(String oddballsPath,
    		File oddballsDir) {
    	
    	List<ArooaDescriptorFactory> descriptors = 
    			new ArrayList<ArooaDescriptorFactory>();
		
		if (oddballsPath != null) {
			descriptors.add(
					new OddballsDescriptorFactory(
							new FileConvertlets().pathToFiles(
									oddballsPath)));
		}
		
		if (oddballsDir != null) {
			descriptors.add(
					new OddballsDirDescriptorFactory(oddballsDir));			
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
    
    /**
     * Configure logging from a log file.
     * 
     * @param logConfigFileName The log file name.
     */
	public void configureLog(String logConfigFileName) {
		System.setProperty("log4j.defaultInitOverride", "true");
	    PropertyConfigurator.configure(logConfigFileName);
		logger().info("Configured logging with file [" + logConfigFileName + "]");
	}
	
	/**
	 * Display usage info.
	 *
	 */
	public void usage() {
	    System.out.println("usage: oddjob [options]");
		System.out.println("-h -help         Displays this usage.");
		System.out.println("-v -version      Displays Oddjobs version.");
	    // only works from Launch.
		System.out.println("-cp -classpath   Extra classpath.");
	    System.out.println("-f -file         job file.");
	    System.out.println("-n -name         Oddjob name.");
	    System.out.println("-l -log          log4j properties file.");
	    System.out.println("-ob -oddballs    Oddballs directory.");
	    System.out.println("-nb -noballs     Run without Oddballs.");
	    System.out.println("-op -obpath      Oddballs path.");
	    System.out.println("--               Pass all remaining arguments through to Oddjob.");
	    
	}
	
	/**
	 * Display version info.
	 *
	 */
	public void version() {
	    System.out.println("Oddjob version: " + new Oddjob().getVersion());
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
	
	/**
	 * Process the properties in oddjob.properties in the users
	 * home directory.
	 * 
	 * @return The properties. Null if there aren't any.
	 * 
	 * @throws IOException
	 */
	protected Properties processUserProperties() throws IOException {
		
		String homeDir = System.getProperty("user.home");		
		
		if (homeDir == null) {
			return null;
		}
			
		File userProperties = new File(homeDir, USER_PROPERTIES);
		
		if (!userProperties.exists()) {
			return null;
		}
			
		Properties props = new Properties();
		InputStream input = new FileInputStream(userProperties);
		
		props.load(input);
		input.close();
		
		return props;
	}
	
	/**
	 * The main.
	 * 
	 * @param args The command line args.
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws IOException {

		Main ojm = new Main();
		OddjobRunner runner = ojm.init(args);
		if (runner == null) {
		    return;
		}
		runner.run();
	}
}
