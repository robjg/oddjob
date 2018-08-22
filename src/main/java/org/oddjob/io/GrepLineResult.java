package org.oddjob.io;

import java.io.File;
import java.io.Serializable;

/**
 * A Java bean to contain the result of a single line match for
 * from the {@link GrepJob}
 * 
 * @author rob
 *
 */
public class GrepLineResult implements Serializable {
	private static final long serialVersionUID = 2012123100L;

	private final File file;
	
	private final int lineNumber;
	
	private final String line;
	
	private final String match;
	
	/**
	 * Constructor for a result not from a file.
	 * 
	 * @param lineNumber
	 * @param line
	 * @param match
	 */
	public GrepLineResult(int lineNumber, String line, String match) {
		this(null, lineNumber, line, match);
	}
	
	/**
	 * Constructor for a result from a file.
	 * 
	 * @param lineNumber
	 * @param line
	 * @param match
	 */
	public GrepLineResult(File file, int lineNumber, 
			String line, String match) {
		this.file = file;
		this.lineNumber = lineNumber;
		
		if (line == null) {
			throw new NullPointerException("Line.");
		}
		this.line = line;
		
		this.match = match;
	}
	
	/**
	 * The file containing the match, if the match was from a file.
	 * 
	 * @return The file. Null if the match was not from a file.
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * The line number in the file or input stream the match was on.
	 * 
	 * @return The line number.
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * The line of text containing the match.
	 * 
	 * @return The line. Never null.
	 */
	public String getLine() {
		return line;
	}
	
	/**
	 * The text section of the line that provided the match. 
	 * 
	 * @return The match. Match will be null if the result was
	 * from an inverted search..
	 */
	public String getMatch() {
		return match;
	}
}
