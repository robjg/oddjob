/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.oddjob.framework.HardReset;
import org.oddjob.framework.SoftReset;


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
	private volatile String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The file, directory, or files and directories
	 * to delete.
	 * @oddjob.required Yes.
	 */
	private volatile File[] files;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Forceably delete non empty directories.
	 * @oddjob.required No, defaults to false.
	 */
	private volatile boolean force;

	/**
	 * 
	 */
	private volatile int logEvery;
	
	private volatile boolean reallyRoot;
	
	private final AtomicInteger fileCount = new AtomicInteger();
	
	private final AtomicInteger dirCount = new AtomicInteger();
	
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

		for (int i = 0; i < toDelete.length; ++i) {
			
			if (Thread.interrupted()) {
				logger.info("Delete interrupted.");
				break;
			}
			
			File fileToDelete = toDelete[i];
			
			if (fileToDelete.getParentFile() == null &&
					!reallyRoot) {
				throw new IllegalStateException(
						"You can not delete root (/*) files " +
						"without setting the reallyRoot property to true.");
			}
			
			if (fileToDelete.isDirectory()) {
				dirCount.incrementAndGet();
			}
			else {
				fileCount.incrementAndGet();
			}
			
			if (force) {
				try {
					FileUtils.forceDelete(fileToDelete);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				if (!fileToDelete.delete()) {
					throw new RuntimeException("Failed to delete " + fileToDelete);
				}
			}
			
			if (logEvery == 1) {
				logger.info("Deleted " + fileToDelete);
			}
			else {
				if (logEvery > 0 && 
						(dirCount.get() + fileCount.get()) % logEvery == 0) {
					logCounts();
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Deleted " + fileToDelete);
				}
			}
		}
		
		logCounts();
	}
	
	
	/**
	 * Log counts.
	 * 
	 */
	private void logCounts() {
		int fileCount = this.fileCount.get();
		int dirCount = this.dirCount.get();
		
		StringBuilder message = new StringBuilder();
		message.append("Deleted ");
		if (fileCount > 0) {
			message.append(fileCount);
			message.append(" file");
			if (fileCount > 1) {
				message.append("s");
			}
			if (dirCount > 0) {
				message.append(" and ");
			}
		}
		if (dirCount > 0) {
			message.append(dirCount);
			message.append(" director");
			if (dirCount == 1) {
				message.append("y");
			}
			else {
				message.append("ies");
			}
		}
		if (fileCount == 0 && dirCount == 0) {
			message.append("nothing");
		}
		message.append(".");
		
		logger.info(message.toString());
	}
	
	@HardReset
	@SoftReset
	public void reset() {
		fileCount.set(0);
		dirCount.set(0);
	}
	
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
	 * Getter for force property.
	 * 
	 * @return The force property.
	 */
	public boolean isForce() {
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
	
	public void setLogEvery(int logEvery) {
		this.logEvery = logEvery;
	}
	
	public int getLogEvery() {
		return logEvery;
	}
	
	public void setReallyRoot(boolean reallyRoot) {
		this.reallyRoot = reallyRoot;
	}
	
	public boolean isReallyRoot() {
		return reallyRoot;
	}
	
	
	public int getFileCount() {
		return fileCount.get();
	}
	
	public int getDirCount() {
		return dirCount.get();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}
}
