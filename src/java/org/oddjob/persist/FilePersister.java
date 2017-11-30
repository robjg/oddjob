/*
 * Copyright © 2004, Rob Gordon.
 */
package org.oddjob.persist;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.registry.Path;

/**
 * @oddjob.description Persist and load jobs from and to a file. The file
 * the job is persisted to is the jobs id with a .ser extension.
 * <p>
 * A new sub directory is created for each nested Oddjob with an id. The
 * job of the nested Oddjob are persisted to the sub directory. Thus the 
 * directory structure mirrors the structure of the Oddjobs.
 * 
 * @oddjob.example 
 * 
 * Using a file persister with Oddjob. The persist directory is passed
 * in as an argument from the command line. The state of child jobs will
 * be saved in a child directory relative to the given directory of the name
 * 'important-jobs'.
 * 
 * {@oddjob.xml.resource org/oddjob/persist/FilePersisterExample.xml}
 * 
 * @author Rob Gordon.
 */
public class FilePersister extends PersisterBase {
	private static final Logger logger = LoggerFactory.getLogger(FilePersister.class);

    /**
     * @oddjob.property dir
     * @oddjob.description The directory in which the files will be created.
     * @oddjob.required No.
     */	
	private File directory;

	/**
	 * Set the directory to save files in.
	 * 
	 * @param dir The directory.
	 */
	@ArooaAttribute
	public void setDir(File dir) {
		this.directory = dir;
	}
	
	/**
	 * Get the directory, files are being persisted in.
	 * 
	 * @return The directory.
	 */
	public File getDir() {
		return this.directory;
	}

	protected void persist(Path path, String id, Object o) 
	throws ComponentPersistException {

		new SerializeWithFile().toFile(directoryFor(path),
				id, o);
	}
	
	protected void remove(Path path, String id) 
	throws ComponentPersistException {
		new SerializeWithFile().remove(directoryFor(path), id);
	}

	protected Object restore(Path path, String id, ClassLoader classLoader) 
	throws ComponentPersistException {
		return new SerializeWithFile().fromFile(
				directoryFor(path), id, classLoader);
	}
	
	@Override
	protected String[] list(Path path) 
	throws ComponentPersistException {
		return new SerializeWithFile().list(
				directoryFor(path));
	}
	
	@Override
	protected void clear(Path path) {
		new SerializeWithFile().clear(new File(directory, path.toString()));
	}	
	
	File directoryFor(Path path) 
	throws ComponentPersistException {
		
		if (directory == null && (
				path == null || path.size() == 0)) {
			throw new NullPointerException(
					"A Path or directory must be provided.");
		}
		
		if (directory != null && !directory.exists()) {
			throw new ComponentPersistException("No directory: " + directory);
		}
		
		File dir = new File(directory, path.toString());
		
		if (!dir.exists()) {
			dir.mkdirs();
			logger.debug("Creating directory [" + dir + "]");
		}
		
		return dir;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + 
			(directory == null ? "" : ". dir=" + directory.getAbsolutePath());
	}
}
