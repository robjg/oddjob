package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;

/**
 * @oddjob.description Make a directory, including any necessary but 
 * nonexistent parent directories. If there already exists a 
 * file with specified name or the directory cannot be created then 
 * an exception is flagged. If the directory exists alread it is left 
 * intact.
 * 
 * @oddjob.example
 * 
 * Make a directory including missing parent directories.
 * 
 * {@oddjob.xml.resource org/oddjob/io/MkdirExample.xml}
 */

public class MkdirJob implements Runnable, Serializable {
	private static final Logger logger = LoggerFactory.getLogger(MkdirJob.class);
	
	private static final long serialVersionUID = 20060117;

	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;

	/** 
	 * @oddjob.property
	 * @oddjob.description The directory to create.
	 * @oddjob.required Yes.
	 */
	private File dir;

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
	public File getDir() {
		return dir;
	}
	
	/**
	 * Set the file.
	 * 
	 * @param The file.
	 */
	@ArooaAttribute
	public void setDir(File file) {
		this.dir = file;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (dir == null) {
			throw new IllegalStateException("File must be specified."); 
		}
		try {
			if (dir.exists()) {
				if (dir.isDirectory()) {
					logger.info("Directory [" + dir + "] exists already.");
				}
				else {
					throw new IllegalArgumentException("File [" +
							dir + "] exists but is not a directory.");
				}
			}
			else {
				FileUtils.forceMkdir(dir);
				logger.info("Created Directory [" + dir + "]");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name ==null) {
			return "Create a Directory"; 
		}
		return name;
	}
}
