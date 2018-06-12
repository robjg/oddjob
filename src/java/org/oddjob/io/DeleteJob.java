/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.oddjob.framework.adapt.HardReset;
import org.oddjob.framework.adapt.SoftReset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
public class DeleteJob implements Callable<Integer>, Serializable {
	private static final long serialVersionUID = 200601172014032400L;

	private static final Logger logger = LoggerFactory.getLogger(DeleteJob.class);
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private volatile String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The file, directory, or files and directories
	 * to delete. Note the files must be valid file name, they can not
	 * contain wildcard characters. This will be the case by default if
	 * the {@link FilesType} is used to specify the files.
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
	 * @oddjob.property
	 * @oddjob.description Logs the number of files and directories deleted
	 * every n number of items. If this property is 1 then the file or
	 * directory path is logged every delete. If this property is less than
	 * one then the counts are logged only at the end.
	 * @oddjob.required No, defaults to 0.
	 */
	private volatile int logEvery;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Flag to indicate that it is the intention to 
	 * delete files at the root level. This is to catch the situation 
	 * where variable substitution is used to specify the file path but
	 * the variable doesn't exists - e.g. The file specification is 
	 * <code>${some.dir}/*</code> but <code>some.dir</code> has not been
	 * defined.
	 * @oddjob.required No, defaults to false.
	 */
	private volatile boolean reallyRoot;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The maximum number of errors to allow before
	 * failing. Sometimes when deleting a large number of files, it is not
	 * desirable to have one or two locked files from stopping all the other
	 * files from being deleted.
	 * @oddjob.required No, defaults to 0.
	 */
	private volatile int maxErrors;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Count of the files deleted. 
	 */
	private final AtomicInteger fileCount = new AtomicInteger();
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Count of the directories deleted. 
	 */
	private final AtomicInteger dirCount = new AtomicInteger();
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Count of the errors. 
	 */
	private final AtomicInteger errorCount = new AtomicInteger();
	
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Integer call() throws IOException, InterruptedException {		
		
		if (files == null) {
			throw new IllegalStateException("Files must be specified."); 
		}
		
		File[] toDelete = files;
		
		try {
			for (int i = 0; i < toDelete.length; ++i) {
				
				if (Thread.interrupted()) {
					throw new InterruptedException("Delete interrupted.");
				}
				
				File fileToDelete = toDelete[i];
				
				if (!fileToDelete.exists()) {
					
					logger.debug("Ignoring " + fileToDelete + 
							", it does not exist.");
					continue;
				}
				
				deleteFile(fileToDelete);
			}
		}
		finally {
			logCounts();
		}
		
		return new Integer(0);
	}
	

	protected void deleteFile(File fileToDelete) throws IOException {
		
		try {
			if (isRoot(fileToDelete) && !reallyRoot) {
				throw new IllegalStateException(
						"You can not delete root (/*) files " +
						"without setting the reallyRoot property to true.");
			}
			
			boolean isDirectory = fileToDelete.isDirectory();
			
			if (force) {
				FileUtils.forceDelete(fileToDelete);
			}
			else {
				if (!fileToDelete.delete()) {
					throw new IOException("Failed to delete " + fileToDelete);
				}
			}
			
			if (isDirectory) {
				dirCount.incrementAndGet();
			}
			else {
				fileCount.incrementAndGet();
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
		catch (IOException|RuntimeException e) {
			if (errorCount.incrementAndGet() >= maxErrors && 
					maxErrors >= 0) {
				if (maxErrors > 0) {
					logger.info("Max error count of " + maxErrors + " exceeded.");
				}
				throw e;
			}
			else {
				logger.info(e.getMessage());
			}
		}
	}
				
	protected boolean isRoot(File fileToDelete) throws IOException {
		File canonicalFile = fileToDelete.getCanonicalFile();
		return canonicalFile.getParentFile() == null || 
				canonicalFile.getParentFile().getParentFile() == null;
	}
	
	/**
	 * Log counts.
	 * 
	 */
	private void logCounts() {
		int fileCount = this.fileCount.get();
		int dirCount = this.dirCount.get();
		int errorCount = this.errorCount.get();
		
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
		if (errorCount > 0) {
			message.append(" (" + errorCount + " error");
			if (errorCount > 1) {
				message.append("s");
			}
			message.append(")");
		}
		message.append(".");
		
		logger.info(message.toString());
	}
	
	@HardReset
	@SoftReset
	public void reset() {
		fileCount.set(0);
		dirCount.set(0);
		errorCount.set(0);
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
	
	public int getErrorCount() {
		return errorCount.get();
	}
	
	public int getMaxErrors() {
		return maxErrors;
	}

	public void setMaxErrors(int maxErrors) {
		this.maxErrors = maxErrors;
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
