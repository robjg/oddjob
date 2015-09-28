/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import org.oddjob.OddjobConsole;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;

/**
 * An implementation of a ConsoleArchiver for the local JVM. An Explorer or Server
 * job will attach this archiver to a job node to capture console output from that
 * node and it's children down to a node which is a different console archiver (
 * because it's a separate process or is getting console output from a remote node).
 * <p>
 * 
 * @author Rob Gordon.
 */
public class LocalConsoleArchiver implements ConsoleArchiver {
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#addConsoleListener(org.oddjob.logging.LogListener, java.lang.Object, long, int)
	 */
	public void addConsoleListener(LogListener l, Object component, 
			long last, int history) {
		archiveFor(component).addListener(l, LogLevel.DEBUG, last, history);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#removeConsoleListener(org.oddjob.logging.LogListener, java.lang.Object)
	 */
	public void removeConsoleListener(LogListener l, Object component) {
		archiveFor(component).removeListener(l);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#consoleIdFor(java.lang.Object)
	 */
	public String consoleIdFor(Object component) {
		return archiveFor(component).getArchive();
	}
	
	private LogArchive archiveFor(Object component) {
		if (component instanceof ConsoleOwner) {
			return ((ConsoleOwner) component).consoleLog();
		}
		else {
			return OddjobConsole.console();
		}		
	}
	
	
	
	/**
	 * Does nothing at the moment.
	 *
	 */
	public void onDestroy() {
	}
		

}
