package org.oddjob.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to parse the class path argument.
 *  
 * @author rob
 *
 */
public class PathParser {

	/** The elements of the class path. */
	private String[] elements;
	
	/**
	 * Get the elements of the class path.
	 * 
	 * @return
	 */
	public String[] getElements() {
		return elements;
	}
	
	/**
	 * Parse the args.
	 * 
	 * @param args The program args.
	 * 
	 * @return The args without the class path args.
	 */
	public String[] processArgs(String[] args) {
		
		List<String> returned = new ArrayList<String>();
		
		String classpath = null;
		
		boolean ignoreRemaining = false;
		for (int i = 0; i < args.length; ++i) {
			
			if ("--".equals(args[i])) {
				ignoreRemaining = true;
			}
			
			if (ignoreRemaining) {
				returned.add(args[i]);
				continue;
			}
			
			if ("-cp".equals(args[i]) || "-classpath".equals(args[i])) {
				if (++i == args.length) {
					throw new IllegalArgumentException("No path argument.");
				}
				classpath = args[i];
				ignoreRemaining = true;
			}
			else {
				returned.add(args[i]);
			}
		}
	
		if (classpath != null) {
			elements = classpath.split(File.pathSeparator);
		}
		else {
			elements = new String[0];
		}
		
		return returned.toArray(new String[returned.size()]);
	}
	
}
