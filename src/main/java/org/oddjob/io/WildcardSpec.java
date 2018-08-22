/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Utility class for wild-card file matching. Note that the File objects
 * returned are all in the canonical file format.
 */
public class WildcardSpec {

    private final File file;        
    
    /**
     * Create a new instance with a String file path.
     * 
     * @param spec
     */
    public WildcardSpec(String spec) {
    	this(new File(spec));
    }
    
    /**
     * Create a new instance with a file.
     * 
     * @param file
     */
    public WildcardSpec(File file) {
    	if (file == null) {
    		throw new NullPointerException("No file.");
    	}
    	this.file = file;
    }
    
    /**
     * Is the file specification a wild card specification.
     * 
     * @return
     */
    public boolean isWildcardSpec() {
    	return !noWildcard(file.getPath());
    }
    
    
    /**
     * Find all files matching the specialisation.
     * 
     * @return
     * @throws IOException 
     */
    public File[] findFiles() throws IOException {
    	DirectorySplit split = new DirectorySplit(file);
    	
    	return findFiles(split);
    }
    
    /**
     * Recursively find file in the path.
     * 
     * @param split The path split up into wildcard sections.
     * 
     * @return
     * @throws IOException 
     */
    protected File[] findFiles(DirectorySplit split) throws IOException {
    	Set<File> results = new TreeSet<File>();
    	
    	File currentFile = split.currentFile();
    	File currentParent = currentFile.getAbsoluteFile().getParentFile();
    	
    	// currentParent will be null when file is "/" 
    	// noWildCard should catch this too - lets be certain.
    	if (currentParent == null) {
        	results.add(currentFile.getCanonicalFile());
    	}
    	else if (currentParent.exists()) {

    		// Find all files matching the current paths name. This
    		// may or may not be a wild card match.
    		List<File> matching = new ArrayList<>();
    		
    		possiblyRecursiveMatch(currentParent, 
    				currentFile.getName(), matching);
    		
    		// for each match, either move down the tree and continue
    		// to match or add the result if we are at the bottom of
    		// the path.
    		for (File match: matching) {
    			
    			if (split.isBottom()) {
    				results.add(match.getCanonicalFile());
    			}
    			else {
    				if (match.isDirectory()) {

    					// recursive call.
    					File[] more = findFiles(
    							split.next(match));
    					results.addAll(Arrays.asList(more));
    				}
    				else {
    					// Ignore if the match is not a directory.
    				}
    			}
    		}
    	}
    	
    	return results.toArray(new File[results.size()]);
    }

    /**
     * Splits a directory into what is above and below the wild cards.
     * <p>
     * Given a file such as <code>a/b/???/x/y/???</code> this will create a 
     * split that is:
     * <ul>
     * <li>a/b/???/x/y/???</li>
     * <li>a/b/???</li>
     * </ul>
     * This is stored as
     * <ul>
     * <li>x/y/???</li>
     * <li>a/b/???</li>
     * </ul>
     * <p>
     * 
     */
    static class DirectorySplit {
    	
        final private LinkedList<File> split;

        /**
         * Create a new instance.
         * 
         * @param file
         */
        DirectorySplit(File file) {

        	if (file == null) {
        		throw new NullPointerException();
        	}
        	
        	split = new LinkedList<File>();

        	File current = file;
        	
    		File parent = current.getParentFile();
    		
        	while (true) {
        		
        		if (parent == null || noWildcard(parent.getPath())) {
        			
            		split.add(current);
            		break;
        		}
        		
        		File below = new File(current.getName());

        		while (true){
        			current = parent;
        			parent = current.getParentFile();
        			
        			if (parent == null || !noWildcard(current.getName())) {
        				break;
        			}

        			below = new File(current.getName(), below.getPath());
        		}
        		
        		split.add(below);
        	}
        }

        /**
         * Create a new instance for {@link #next(String)}.
         * 
         * @param previous
         * @param name
         * @throws IOException 
         */
        private DirectorySplit(DirectorySplit previous, File newParent) throws IOException { 
        	split = new LinkedList<File>(previous.split);
        	split.removeLast().getParentFile(); // remove the last;
        	String nextDown = split.removeLast().getPath(); // replace the current;
        	split.add(new File(newParent, nextDown));
        }

        File getParentFile() throws IOException {
        	File parent = split.getLast().getParentFile();
        	return parent;
        }
        
        File currentFile() {
        	return split.getLast();
        }
        
        String getName() {
        	return split.getLast().getName();
        }
        
        boolean isBottom() {
        	return split.size() == 1;
        }
        
        int getSize() {
        	return split.size();
        }
        
        DirectorySplit next(File newParent) throws IOException {
        	if (split.size() == 1) {
        		return null;
        	}
        	
        	DirectorySplit next = new DirectorySplit(this, newParent);
    		return next;
        }
    }
        
    static boolean noWildcard(String text) {
		return text.indexOf('*') < 0 &&
				text.indexOf('?') < 0;
	}
	
    public static void possiblyRecursiveMatch(final File from, 
    		final String namespec,  final List<File> results) {

    	String singleWildcards = namespec.replace("**", "*");
    	
		File[] matching = from.listFiles(
				(FileFilter) new WildcardFileFilter(singleWildcards, IOCase.SYSTEM));
    	
		if (matching == null) {
			throw new IllegalArgumentException("Can't list files for directory " +
					from + " and name spec " + singleWildcards);
		}
		
		results.addAll(Arrays.asList(matching));
		
		if (singleWildcards.equals(namespec)) {
			return;
		}

		if ("*".equals(singleWildcards) && results.size() == matching.length) {
			results.add(0, from);
		}
		
		for (File match : matching) {
			if (match.isDirectory()) {
				possiblyRecursiveMatch(match, namespec, results);
			}
		}
    }    

}
