package org.oddjob.io;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;


/**
 * @oddjob.description Test if a file exists. This job will flag
 * complete if the file exists, not complete if it doesn't, and 
 * will signal an exception if the path to the file does not exist.
 * 
 * @oddjob.example
 * 
 * A simple example checking for a single file.
 * 
 * {@oddjob.xml.resource org/oddjob/io/ExistsSimpleExample.xml}
 * 
 * @oddjob.example
 * 
 * File polling.
 * 
 * {@oddjob.xml.resource org/oddjob/io/ExistsFilePollingExample.xml}
 * 
 * @oddjob.example
 * 
 * Using exists and processing the files found.
 * 
 * {@oddjob.xml.resource org/oddjob/io/ExistsWithFilesExample.xml}
 * 
 */
public class ExistsJob implements Runnable, Serializable {
	private static final long serialVersionUID = 20060117;

	private static final Logger logger = Logger.getLogger(ExistsJob.class);
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;

	/** 
	 * @oddjob.property
	 * @oddjob.description The file, can contain wildcard characters.
	 * @oddjob.required Yes.
	 */
	private File file;

	/** 
	 * @oddjob.property
	 * @oddjob.description The files that match the file spec.
	 * @oddjob.required R/O.
	 */
	private File[] exists;
		
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
	 * Get the file.
	 * 
	 * @return The file.
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Set the file.
	 * 
	 * @param The file.
	 */
	@ArooaAttribute
	public void setFile(File file) {
		this.file = file;
	}

	public File[] getExists() {
		return exists;
	}
	
	/**
	 * Get the result. Used to set complete/not state.
	 * 
	 * @return 0 if file exists.
	 */
	public int getResult() {
		if (exists == null) {
			return -1;
		}
		return exists.length > 0 ? 0 : 1;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (file == null) {
			throw new IllegalStateException("File must be specified."); 
		}

		logger.info("Finding files matching " + file);
		
		WildcardSpec wild = new WildcardSpec(file);
		exists = wild.findFiles();
		
		if (exists.length == 0) {
			logger.info("No Files found.");
		}

		for (File found: exists) {
			logger.info("" + found);
		}
	}
	
	/**
	 * @oddjob.property size
	 * @oddjob.description If a single file is found, this is the size
	 * of the file in bytes, or -1 if a single file hasn't been found.
	 * @oddjob.required R/O.
	 */
	public long getSize() {
		File[] exists = this.exists;
		if (exists == null || exists.length != 1) {
			return -1;
		}
		
		return exists[0].length();
	}
	
	/**
	 * @oddjob.property lastModified
	 * @oddjob.description If a single file is found, this is the last
	 * modified date of the file.
	 * @oddjob.required R/O.
	 */
	public Date getLastModified() {
		File[] exists = this.exists;
		if (exists == null || exists.length != 1) {
			return null;
		}
		
		return new Date(exists[0].lastModified());
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return "Check File Exists";
		}
		return name;
	}
}
