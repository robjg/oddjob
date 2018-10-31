/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Utility methods for dealing with files.
 * 
 * @author Rob Gordon
 */
public class FilesUtil {

	/**
	 * Expand a file that could contain wild cards.
	 * 
	 * @param file The file.
	 * 
	 * @return An array of files. Never null.
	 * @throws IOException 
	 */
	public static File[] expand(File file) throws IOException {
		return new WildcardSpec(file).findFiles();
	}
	
	/**
	 * Expand an array of files which could contain wildcards into 
	 * an actual array of files. The array will be sorted.
	 * 
	 * @param files The in files.
	 * @return The expanded files
	 * @throws IOException 
	 */
	public static File[] expand(File[] files) throws IOException {
		SortedSet<File> results = new TreeSet<File>();
		for (int i = 0; i < files.length; ++i) {
			results.addAll(Arrays.asList(expand(files[i])));
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
	 * Verify that a file is writeable.
	 * 
	 * @param file A file.
	 * @throw RuntimeException if one of the files isn't writeable.
	 */
	public static void verifyWrite(File file) 
	throws RuntimeException {
		if (!file.exists()) {
			throw new RuntimeException("File " + file + " does not exist.");
		}
		if (!file.canWrite()) {
			throw new RuntimeException("File " + file + " can not be changed.");
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
			verifyWrite(files[i]);
		}		
	}
}
