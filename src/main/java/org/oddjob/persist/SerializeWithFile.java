/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.persist;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.life.ComponentPersistException;

/**
 * Serialize an object to and from file with the given name. The .ser extension
 * is added to the file name.
 * <p>
 * When writing a temporary name is used in case the process is kill mid writing
 * so there is less chance of a corrupted file. The temporary name includes an
 * underscore in the name.
 * 
 * @author Rob Gordon.
 */
public class SerializeWithFile {
	private static final Logger logger = LoggerFactory.getLogger(SerializeWithFile.class);

	private final static String EXTENSION = ".ser";

	public void toFile(File dir, String name, Object o) 
	throws ComponentPersistException {
		
		File inProgress = new File(dir, name + "_"+ EXTENSION);
		File old = new File(dir, "_" + name + EXTENSION);
		File finished = new File(dir, name + EXTENSION);
		
		ObjectOutput oo = null;
		try {
			OutputStream os = new FileOutputStream(inProgress);
			oo = new ObjectOutputStream(os);
		}
		catch (FileNotFoundException e) {
			throw new ComponentPersistException("Check directory exists!", e);
		}
		catch (IOException e) {
			throw new ComponentPersistException("Failed opening file [" +
					inProgress + "]", e);
		}
		
		try {
			oo.writeObject(o);			
		}
		catch (IOException e) {
			throw new ComponentPersistException("Failed writing object id ["
					+ name + "], class [" + o.getClass().getName() 
					+ "], object [" + o + "]."
					, e);
		}
		finally {
			try {
				oo.close();
			}
			catch (IOException e) {
				// ignore
			}
		}
		
		// Need to move the old file because on some platforms you can't
		// rename over an existing file.
		if (finished.exists()) {
			if (!finished.renameTo(old)) {
				logger.warn("Failed renaming " + finished + " to " +
							old);
			}
		}
		
		if (!inProgress.renameTo(finished)) {
			throw new ComponentPersistException("Failed renaming " + inProgress + 
					" to " + finished);
		}
		
		// Now delete the old file for next time.
		if (old.exists()) {
			if (!old.delete()) {
				logger.warn("Failed deleting " + old);			
			}
		}
		
		logger.debug("Saved [" + o + "], id [" + name 
		        + "] to file ]["
				+ finished + "].");
	}
	
	public void remove(File dir, String name) {
		File f = new File(dir, name + EXTENSION);
		if (f.exists()) {
			boolean result = f.delete();
			if (result) {
				logger.debug("Deleted [" + f + "].");
			}
			else {
				logger.debug("Failed to delete [" + f + "].");
			}
		}
	}

	public Object fromFile(File dir, String name, ClassLoader classLoader) 
	throws ComponentPersistException {
		
		File f = new File(dir, name + EXTENSION);
		if (!f.exists()) {
			return null;
		}
		
		ObjectInput oi = null;
		try {
			InputStream is = new FileInputStream(f);
			oi = new OddjobObjectInputStream(is, classLoader);
		}
		catch (IOException e) {
			throw new ComponentPersistException(
					"Failed opening file [" + f + "]", e);
		}
		
		try {
			Object o = oi.readObject();

			logger.debug("Loaded [" + o + "] from [" + f + "].");

			return o;
		}
		catch (Exception e) {
			throw new ComponentPersistException(
					"Failed reading component from file " + f, e);
		}
		finally {
			try {
				oi.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	public void clear(File dir) {
		if (dir.exists()) {
			try {
				FileUtils.forceDelete(dir);
				logger.debug("Deleted [" + dir + "].");
			} catch (IOException e) {
				throw new RuntimeException("Failed to delete [" + 
						dir + "].", e);
			}
		}
	}
	
	public String[] list(File dir) {
		
		File[] files = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isFile() && pathname.getName().endsWith(EXTENSION)) {
					return true;
				}
				else {
					return false;
				}
			}
		});	

		String[] names = new String[files.length];
		
		for (int i = 0 ; i < names.length ; ++i) {
			String nameWithExtension = files[i].getName();
			names[i] = nameWithExtension.substring(0, 
					nameWithExtension.length() - EXTENSION.length());
		}
		
		logger.debug("Listing [" + names.length + "] file from [" +
				dir + "]");
		
		return names;
	}	
	
	public String[] children(File dir) {
		
		File[] files = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}
				else {
					return false;
				}
			}
		});	

		String[] names = new String[files.length];
		
		for (int i = 0 ; i < names.length ; ++i) {			
			names[i] = files[i].getName();
		}
		
		logger.debug("Children [" + names.length + "] of [" +
				dir + "]");
		
		return names;
	}	
}
