/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;

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
    File[] findFiles(DirectorySplit split) throws IOException {
    	Set<File> results = new TreeSet<File>();
    	
    	final File currentFile = split.currentFile();
    	File currentParent = currentFile.getAbsoluteFile().getParentFile();
    	
    	// currentParent will be null when file is "/" although
    	// noWildCard should catch this too - lets be certain.
    	if (currentParent == null || noWildcard(currentFile.getName())) {
        	results.add(currentFile.getCanonicalFile());
    	}
    	else {

    		// Find all files matching the current paths name. This
    		// may or may not be a wild card match.
    		File[] matching = currentParent.listFiles(
    				new FileFilter() {
    			public boolean accept(File pathname) {
    				return FilenameUtils.wildcardMatchOnSystem(
    						pathname.getName(), currentFile.getName());
    			}
    		});
    		
    		
    		// for each match, either move down the tree and continue
    		// to match or add the result if we are at the bottom of
    		// the path.
    		for (int i = 0; matching != null && i < matching.length; ++i) {
    			if (split.isBottom()) {
    				results.add(matching[i].getCanonicalFile());
    			}
    			else {
    				if (matching[i].isDirectory()) {

    					// recursive call.
    					File[] more = findFiles(
    							split.next(matching[i].getName()));
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
        private DirectorySplit(DirectorySplit previous, String name) throws IOException { 
        	split = new LinkedList<File>(previous.split);
        	File previousParent = split.removeLast().getParentFile(); // remove the last;
        	String currentName = split.removeLast().getPath(); // replace the current;
        	File wildcardPathReplaced = 
        			new File(previousParent, name);
        	split.add(new File(wildcardPathReplaced, currentName));
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
        
        DirectorySplit next(String name) throws IOException {
        	if (split.size() == 1) {
        		return null;
        	}
        	
        	DirectorySplit next = new DirectorySplit(this, name);
    		return next;
        }
    }
        
    static boolean noWildcard(String text) {
		return text.indexOf('*') < 0 &&
				text.indexOf('?') < 0;
	}
	
    // taken as is from ant...
    
    /**
     * Tests whether or not a string matches against a pattern.
     * The pattern may contain two special characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern The pattern to match against.
     *                Must not be <code>null</code>.
     * @param str     The string which must be matched against the pattern.
     *                Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed
     *                        case sensitively.
     *
     *
     * @return <code>true</code> if the string matches against the pattern,
     *         or <code>false</code> otherwise.
     */
    public static boolean match(String pattern, String str,
                                boolean isCaseSensitive) {
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;

        boolean containsStar = false;
        for (int i = 0; i < patArr.length; i++) {
            if (patArr[i] == '*') {
                containsStar = true;
                break;
            }
        }

        if (!containsStar) {
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (ch != '?') {
                    if (isCaseSensitive && ch != strArr[i]) {
                        return false; // Character mismatch
                    }
                    if (!isCaseSensitive && Character.toUpperCase(ch)
                            != Character.toUpperCase(strArr[i])) {
                        return false;  // Character mismatch
                    }
                }
            }
            return true; // String matches against pattern
        }

        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?') {
                if (isCaseSensitive && ch != strArr[strIdxStart]) {
                    return false; // Character mismatch
                }
                if (!isCaseSensitive && Character.toUpperCase(ch)
                        != Character.toUpperCase(strArr[strIdxStart])) {
                    return false; // Character mismatch
                }
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?') {
                if (isCaseSensitive && ch != strArr[strIdxEnd]) {
                    return false; // Character mismatch
                }
                if (!isCaseSensitive && Character.toUpperCase(ch)
                        != Character.toUpperCase(strArr[strIdxEnd])) {
                    return false; // Character mismatch
                }
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (ch != '?') {
                        if (isCaseSensitive && ch != strArr[strIdxStart + i
                                + j]) {
                            continue strLoop;
                        }
                        if (!isCaseSensitive
                            && Character.toUpperCase(ch)
                                != Character.toUpperCase(strArr[strIdxStart + i + j])) {
                            continue strLoop;
                        }
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }

}
