package org.oddjob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;


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
	
	public static final String USER_PROPERTIES = "oddjob.properties";

	/**
	 * Parse the command args and configure Oddjob.
	 * 
	 * @param args The args.
	 * @return A configured and ready to run Oddjob.
	 * @throws FileNotFoundException 
	 */
    public Oddjob init(String args[]) throws IOException {
	    
    	OddjobBuilder oddjobBuilder = new OddjobBuilder();
    	
    	Properties props = processUserProperties();
    	
		String logConfig = null;
		
		String oddjobHome = System.getProperty("oddjob.home");
		oddjobBuilder.setOddjobHome(oddjobHome);
		
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
				oddjobBuilder.setName(args[++i]);
				startArg += 2;
			} else if (arg.equals("-l") || arg.equals("-log")) {
			    logConfig = args[++i];
				startArg += 2;
			} else if (arg.equals("-f") || arg.equals("-file")) {
				oddjobBuilder.setOddjobFile(args[++i]);
				startArg += 2;
			} else if (arg.equals("-nb") || arg.equals("-noballs")) {
				oddjobBuilder.setNoOddballs(true);
				startArg += 1;
			} else if (arg.equals("-ob") || arg.equals("-oddballs")) {
				oddjobBuilder.setOddballsDir(new File(args[++i]));
				startArg += 2;
			} else if (arg.equals("-op") || arg.equals("-obpath")) {
				oddjobBuilder.setOddballsPath(args[++i]);
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
		
		Oddjob oddjob = oddjobBuilder.buildOddjob();
		
		oddjob.setProperties(props);
		
		// pass remaining args into Oddjob.
		Object newArray = Array.newInstance(String.class, args.length - startArg);
	    System.arraycopy(args, startArg, newArray, 0, args.length - startArg);
	    oddjob.setArgs((String[]) newArray);
	    	    
		return oddjob;
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
	    System.out.println("-f -file         Job file. Defaults to oddjob.xml");
	    System.out.println("-n -name         Oddjob name. Used in logging.");
	    System.out.println("-l -log          log4j properties file.");
	    System.out.println("-ob -oddballs    Oddballs directory. Defaults to ${oddjob.home}/oddballs");
	    System.out.println("-nb -noballs     Run without Oddballs from the oddballs direcotry.");
	    System.out.println("-op -obpath      Oddballs path. Oddballs that suppliment those in the Oddball directory.");
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
		
		Oddjob oddjob = ojm.init(args);
		
		if (oddjob == null) {
		    return;
		}
		
		OddjobRunner runner = new OddjobRunner(oddjob);
		runner.initShutdownHook();
		
		runner.run();
	}
}
