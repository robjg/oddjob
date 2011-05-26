/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

/**
 * Something that archives console output.
 */
public interface ConsoleOwner {

	/**
	 * Provide the console archive.
	 * 
	 * @return The archive. Must not be null.
	 */
	public LogArchive consoleLog();	
}
