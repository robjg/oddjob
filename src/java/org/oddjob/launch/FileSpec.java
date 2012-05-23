/*
 * Based on code from Ant.
 */
package org.oddjob.launch;

import java.io.File;
import java.io.FileFilter;

/**
 * Provide file matching for deriving the class path.
 * 
 * @author Rob Gordon.
 */
public class FileSpec {

	/** The file specification to match against. */
    private final File filespec;
    
    /** Is the match case sensitive */
    private final boolean caseSensative;
            
    /**
     * Constructor.
     * 
     * @param filespec
     */
    public FileSpec(File filespec) {
    	this(filespec, false);
    }

    /**
     * Constructor
     * 
     * @param filespec
     * @param caseSensative
     */
    public FileSpec(File filespec, boolean caseSensative) {
        this.filespec = filespec;
        this.caseSensative = caseSensative;
	}
    
    /**
     * Get the files matching the filespec.
     *
     * @return An array of all files.
     */
    public File[] getFiles() {
    	
    	// if it's a directory it's something like 'classes' so return
    	// it straight away.
    	if (filespec.isDirectory()) {
    		return new File[] { filespec };
    	}
    	
        File parentDir = filespec.getParentFile();
        
        // if the parent directory is null, it's something like *.jar so
        // use the current directory.
        if (parentDir == null) {
        	parentDir = new File(System.getProperty("user.dir"));
        	
        }
        
        File[] result = parentDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                	
                    return match(filespec.getName(), pathname.getName(), 
                    		caseSensative);
                }
            });
        
        // Ignore errors.
        if (result == null) {
        	return new File[0];
        }
        
        // if we used the current directory we need to restore the
        // list to be just names. Example *.jar becomes a.jar b.jar
        // not /home/rob/a.jar /home/rob/b.jar etc.
        if (filespec.getParentFile() == null) {
        	for (int i = 0 ; i < result.length; ++i) {
        		result[i] = new File(result[i].getName());
        	}
        }
        
        return result;
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
