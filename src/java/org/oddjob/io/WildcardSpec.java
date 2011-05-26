/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/**
 */
public class WildcardSpec {

    private File file;        
    
    public WildcardSpec(String spec) {
    	this(new File(spec));
    }
    
    public WildcardSpec(File file) {
    	this.file = file;
    }
    
    public File[] findFiles() {
    	DirectorySplit split = new DirectorySplit(file);
    	return findFiles(split);
    }
    
    public File[] findFiles(final DirectorySplit split) {
    	Set<File> results = new HashSet<File>();
    	if (split.getParentFile() == null) {
    		// should only happen with the "/" spec.
    		results.add(new File(split.getName()));
    	}
    	else {
    		File[] matching = split.getParentFile().listFiles(new FileFilter() {
    			public boolean accept(File pathname) {
    				return FilenameUtils.wildcardMatchOnSystem(pathname.getName(), split.getName());
    			}
    		});
    		for (int i = 0; matching != null && i < matching.length; ++i) {
    			if (!split.isBottom()) {
    				if (matching[i].isDirectory()) {
    					File[] more = findFiles(split.next(matching[i].getName()));
    					results.addAll(Arrays.asList(more));
    				}
    			}
    			else {
    				results.add(matching[i]);
    			}
    		}
    	}
    	return (File[]) results.toArray(new File[0]);
    }

    static class DirectorySplit {
        LinkedList<AboveAndBelow> split = 
        	new LinkedList<AboveAndBelow>();

        private DirectorySplit() { }
        
        DirectorySplit(File file) {
    		for (AboveAndBelow ab = new AboveAndBelow(file); 
    				true; ab = new AboveAndBelow(ab)) {
    			split.add(ab);
    			if (ab.top) {
    				break;
    			}
    			if (ab.parent.getPath().indexOf('*') < 0 &&
    					ab.parent.getPath().indexOf('?') < 0) {
    				break;
    			}
    		}
        }
       
        File getParentFile() {
        	File parent = ((AboveAndBelow) split.getLast()).parent;
        	return parent;
        }
        
        String getName() {
        	return ((AboveAndBelow) split.getLast()).name;
        }
        
        boolean isBottom() {
        	return ((AboveAndBelow) split.getLast()).below == null;
        }
        
        int getSize() {
        	return split.size();
        }
        
        DirectorySplit next(String name) {
        	if (split.size() == 1) {
        		return null;
        	}
        	
        	DirectorySplit next = new DirectorySplit();
        	next.split = new LinkedList<AboveAndBelow>(split);
        	next.split.removeLast();
        	((AboveAndBelow) next.split.getLast()).parent = new File(getParentFile(), name);
    		return next;
        }
    }
        
    static class AboveAndBelow {
    	File parent;
    	String name;
    	File below;
    	boolean top;
    	
    	AboveAndBelow(AboveAndBelow previous) {
    		if (previous.top) {
    			throw new IllegalStateException("Previous was top.");
    		}
    		if (previous.parent == null) {
    			throw new IllegalStateException("Previous should have been top.");
    		}
    		
    		parent = previous.parent.getParentFile();
    		if (parent == null) {
        		if (previous.parent.isAbsolute()) {
        			throw new IllegalStateException("Previous should have been top.");        			
        		}
				parent = previous.parent.getAbsoluteFile().getParentFile();
				top = true;        		
    		}
    		else {
        		if (parent.getAbsoluteFile().getParentFile() == null) {
        			// path must be "/"
    				top = true;
    			}
    		}
    		
    		name = previous.parent.getName();

    		if (previous.below == null) {
    			below = new File(previous.name);
    		}
    		else {
    			below = new File(previous.name, previous.below.getPath());    			
    		}
    	}
    	
    	AboveAndBelow(File first) {
    		parent = first.getParentFile();
    		if (parent == null) {
    			// parent will be null for "/" situation.
    			parent = first.getAbsoluteFile().getParentFile();
    			top = true;
    		}
    		name = first.getName();
    		below = null;
    	}
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
