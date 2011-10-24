package org.oddjob.io;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;


/**
 * @oddjob.description Rename a file or directory.
 * <p>
 * This is a simple wrapper for Java's File.rename method and so is very
 * limited.
 * 
 * @oddjob.example
 * 
 * Rename a file and rename it back.
 * 
 * {@oddjob.xml.resource org/oddjob/io/RenameExample.xml}
 * 
 * 
 * @author rob
 */
public class RenameJob implements Runnable, Serializable {
	private static final long serialVersionUID = 20060117;
	
	private static final Logger logger = Logger.getLogger(RenameJob.class);
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The from file.
	 * @oddjob.required Yes.
	 */
	private File from;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The to file.
	 * @oddjob.required Yes.
	 */
	private File to;
		
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the from file.
	 * 
	 * @return The from file.
	 */
	public File getFrom() {
		return from;
	}
	
	/**
	 * Set the from file.
	 * 
	 * @param The from file.
	 */
	@ArooaAttribute
	public void setFrom(File file) {
		this.from = file;
	}

	/**
	 * Get the to file.
	 * 
	 * @return The to file.
	 */
	public File getTo() {
		return to;
	}
	
	/**
	 * Set the to file.
	 * 
	 * @param The to file.
	 */
	@ArooaAttribute
	public void setTo(File file) {
		this.to = file;
	}


	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (from == null) {
			throw new NullPointerException("From file must be specified.");
		}
		if (to == null) {
			throw new NullPointerException("To file must be specified.");
		}
		if (!from.exists()) {
			throw new RuntimeException("[" + from + "], no such file or directory.");
		}
		if (!from.renameTo(to)) {
			throw new RuntimeException("Rename from " + from + " to " +
					to + " failed.");
		}
		else {
			logger.info("Renamed " + from + " to " +
					to);
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return "Rename";
		}
		return name;
	}
}
