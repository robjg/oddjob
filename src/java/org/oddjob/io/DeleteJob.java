/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


/**
 * @oddjob.description Delete a file or directory, or files 
 * and directories.
 * <p>
 * Unless the force property is set, this job will cause an
 * exception if an attempt is made to delete a non empty directory.
 * 
 * @oddjob.example
 * 
 * Delete all files from a directory. The directory is the first of
 * Oddjob's arguments.
 * 
 * {@oddjob.xml.resource org/oddjob/io/DeleteFilesExample.xml}
 * 
 * @author Rob Gordon
 */
public class DeleteJob implements Runnable, Serializable {
	private static final long serialVersionUID = 20060117;

	private static final Logger logger = Logger.getLogger(DeleteJob.class);
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The file, directory, or files and directories
	 * to delete.
	 * @oddjob.required Yes.
	 */
	private File[] files;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Forceably delete non empty directories.
	 * @oddjob.required No, defaults to false.
	 */
	private boolean force;
	
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
	 * Get the files.
	 * 
	 * @return The files.
	 */
	public File[] getFiles() {
		return files;
	}
	
	/**
	 * Set the files.
	 * 
	 * @param The files.
	 */
	public void setFiles(File[] files) {
		this.files = files;
	}

	/**
	 * Geter for force property.
	 * 
	 * @return The force property.
	 */
	public boolean getForce() {
		return force;
	}
	
	/**
	 * Setter for force property.
	 * 
	 * @param force The force property.
	 */
	public void setForce(boolean force) {
		this.force = force;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {		
		if (files == null) {
			throw new IllegalStateException("Files must be specified."); 
		}
		File[] toDelete = Files.expand(files);
		Files.verifyWrite(toDelete);

		int fileCount = 0;
		int dirCount = 0;
		for (int i = 0; i < toDelete.length; ++i) {
			if (toDelete[i].isDirectory()) {
				++dirCount;
			}
			else {
				++fileCount;
			}
			if (force) {
				try {
					FileUtils.forceDelete(toDelete[i]);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				if (!toDelete[i].delete()) {
					throw new RuntimeException("Failed to delete " + toDelete[i]);
				}
			}
			logger.debug("Deleted " + toDelete[i]);
		}
		logger.info("Deleted " + fileCount + " files, " 
				+ dirCount + " directories.");
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return "Delete Files and Directories";
		}
		else {
			return name;
		}
	}
}
