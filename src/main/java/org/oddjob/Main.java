package org.oddjob;

import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.utils.Try;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;


/**
 * This is a very simple wrapper around Oddjob with a main method.
 * 
 * @author Rob Gordon
 */
public class Main {
	private static Logger logger;

	private static Logger logger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(Main.class);
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
    public Supplier<Try<Oddjob>> init(String[] args) throws IOException {
	    
    	OddjobBuilder oddjobBuilder = new OddjobBuilder();
    	
    	Properties props = processUserProperties();
    	
		String logConfig = null;
		
		String oddjobHome = System.getProperty("oddjob.home");
		oddjobBuilder.setOddjobHome(oddjobHome);
		
		int startArg = 0;
		
		// cycle through given args
		label:
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			switch (arg) {
				case "-h":
				case "-help":
					usage();
					return () -> Try.of(null);
				case "-v":
				case "-version":
					version();
					return () -> Try.of(null);
				case "-n":
				case "-name":
					oddjobBuilder.setName(args[++i]);
					startArg += 2;
					break;
				case "-l":
				case "-log":
					logConfig = args[++i];
					startArg += 2;
					break;
				case "-f":
				case "-file":
					oddjobBuilder.setOddjobFile(args[++i]);
					startArg += 2;
					break;
				case "-nb":
				case "-noballs":
					oddjobBuilder.setNoOddballs(true);
					startArg += 1;
					break;
				case "-ob":
				case "-oddballs":
					oddjobBuilder.setOddballsDir(new File(args[++i]));
					startArg += 2;
					break;
				case "-op":
				case "-obpath":
					oddjobBuilder.setOddballsPath(args[++i]);
					startArg += 2;
					break;
				case "--":
					startArg += 1;
					break label;
				default:
					// unrecognised arg, so also pass though to oddjob;
					break label;
			}
		}

		if (logConfig != null) {
			configureLog(logConfig);
		}
				
		// pass remaining args into Oddjob.
		String[] newArray = new String[args.length - startArg];
	    System.arraycopy(args, startArg, newArray, 0, args.length - startArg);

	    return () -> {
		
			Try<Oddjob> oddjob = oddjobBuilder.buildOddjob();
			
			oddjob.map(oj -> {
				oj.setProperties(props);
	
			    oj.setArgs(newArray);
	
			    return oj;
			});
		
			return oddjob;
		};
	}

    /**
     * Configure logging from a log file.
     * 
     * @param logConfigFileName The log file name.
     */
	public void configureLog(String logConfigFileName) {
		LoggerAdapter.configure(logConfigFileName);
		
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
			logger().debug("No user.home System property. Not attempting to find User Properties.");
			return null;
		}
			
		File userProperties = new File(homeDir, USER_PROPERTIES);
		
		if (!userProperties.exists()) {
			logger().debug("No User Property File: {}", userProperties);
			return null;
		}

		logger().debug("Loading User Properties from: {}", userProperties);

		Properties props = new Properties();
		InputStream input = Files.newInputStream(userProperties.toPath());
		
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
	public static void main(String[] args) throws Exception {

		Main ojm = new Main();
		
		Supplier<Try<Oddjob>> sup = ojm.init(args);
		
		try (Restore restore = OddjobNDC.push(logger().getName(), Main.class.getSimpleName())) {
		

			Try<Oddjob> tryOj = sup.get();
			
			Optional<Oddjob> optOj = tryOj.map(Optional::ofNullable)
					.orElseThrow();
			
					
					
			optOj.ifPresent(oj -> { OddjobRunner runner = new OddjobRunner(oj);					                    
						            runner.initShutdownHook();
						            runner.run();
						          });
		}
	}
}
