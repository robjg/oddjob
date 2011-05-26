/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for dealing with files.
 * 
 * @author Rob Gordon
 */
public class Files {

	/**
	 * Expand an array of files which could contain wildcards into 
	 * an actual array of files.
	 * 
	 * @param files The in files.
	 * @return The expanded files
	 */
	public static File[] expand(File[] files) {
		Set<File> results = new HashSet<File>();
		for (int i = 0; i < files.length; ++i) {
			results.addAll(Arrays.asList(new WildcardSpec(files[i]).findFiles()));
		}
    	return (File[]) results.toArray(new File[0]);
	}
	
	/**
	 * Verify that all files are readable.
	 * 
	 * @param files The files.
	 * @throw RuntimeExceptoin if on isn't readable.
	 */
	public static void verifyReadable(File[] files) 
	throws RuntimeException {
		for (int i = 0; i < files.length; ++i) {
			if (!files[i].exists()) {
				throw new RuntimeException("File " + files[i] + " does not exist.");
			}
			if (!files[i].canRead()) {
				throw new RuntimeException("File " + files[i] + " can not be read.");
			}
		}		
	}

	/**
	 * Verify that files are writeable.
	 * 
	 * @param files The files.
	 * @throw RuntimeException if one of the files isn't writeable.
	 */
	public static void verifyWrite(File[] files) 
	throws RuntimeException {
		for (int i = 0; i < files.length; ++i) {
			if (!files[i].exists()) {
				throw new RuntimeException("File " + files[i] + " does not exist.");
			}
			if (!files[i].canWrite()) {
				throw new RuntimeException("File " + files[i] + " can not be changed.");
			}
		}		
	}
}
