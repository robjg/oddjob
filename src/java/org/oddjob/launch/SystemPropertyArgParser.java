package org.oddjob.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Process -D arguments passed to the Oddjob. This mimics the behaviour of
 * -D properties passed to the JVM and set These are set as System
 * properties.
 * 
 * @author rob
 *
 */
public class SystemPropertyArgParser {

	private final Pattern pattern;
	
	/** 
	 * Constructor. 
	 */
	public SystemPropertyArgParser() {
		pattern = Pattern.compile("-D([^=]+)=(.*)");
	}
	
	
	/**
	 * Parse the args.
	 * 
	 * @param args The program args.
	 * 
	 * @return The args without system properties.
	 */
	public String[] processArgs(String[] args) {
		
		List<String> returned = new ArrayList<String>();
		
		boolean ignore = false;
		for (int i = 0; i < args.length; ++i) {
			
			if ("--".equals(args[i])) {
				ignore = true;
			}
			
			if (ignore) {
				returned.add(args[i]);
				continue;
			}
			
			Matcher match = pattern.matcher(args[i]);
			if (!match.matches()) {
				returned.add(args[i]);
				continue;
			}
			
			String property = match.group(1);
			String value = match.group(2);
			
			System.setProperty(property, value);
		}
	
		return returned.toArray(new String[returned.size()]);
	}
}
