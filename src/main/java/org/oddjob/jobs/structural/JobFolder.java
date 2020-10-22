package org.oddjob.jobs.structural;


import org.oddjob.FailedToStopException;
import org.oddjob.Iconic;
import org.oddjob.Structural;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageData;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;
import java.io.IOException;

/**
 * @oddjob.description Holds a collection of jobs but does not
 * execute them. Used to collect and organise jobs. The jobs can either
 * be scheduled by a scheduler or run manually.
 * <p>
 * A folder has no state, it can't be run and it can't be stopped.
 * 
 * @oddjob.example
 * 
 * A folder of jobs.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/JobFolderExample.xml}
 * 
 * 
 * @author Rob Gordon 
 */
public class JobFolder 
			implements Structural, Iconic {

	private static final Logger logger = LoggerFactory.getLogger(JobFolder.class);
	
	/** Icon. */
	private static final ImageData icon;
	static {
		try {
			icon = ImageData.fromUrl(
					IconHelper.class.getResource("Open16.gif"),
					"folder");
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	/** Child helper. */
	protected ChildHelper<Object> childHelper
			= new ChildHelper<>(this);
			
	/** 
	 * @oddjob.property
	 * @oddjob.description The name of the folder.
	 * @oddjob.required No. 
	 */
	private String name;

	/**
	 * This flag is set once the object is destroyed
	 * Methods in subclass should check this flag.
	 */
	protected transient volatile boolean destroyed;
	
	/**
	 * Set the job name. Used by subclasses to set the job name.
	 * 
	 * @param name The name of the job.
	 */
	synchronized public void setName(String name) {
		if (destroyed) {
			throw new IllegalStateException("[" + this + "] destroyed");
		}
		this.name = name;
	}

	/**
	 * Get the job name.
	 * 
	 * @return The job name.
	 */
	synchronized public String getName() {
		return name;
	}
		
	/**
	 * Add a child.
	 * 
	 * @oddjob.property jobs
	 * @oddjob.description The jobs.
	 * @oddjob.required No.
	 * 
	 * @param child A child
	 */
	public void setJobs(int index, Object child) {
		if (child == null) {
			childHelper.removeChildAt(index);
		}
		else {
			childHelper.insertChild(index, child);
		}
	}	

	/**
	 * Add a listener. The listener will immediately receive add
	 * notifications for all existing children.
	 * 
	 * @param listener The listener.
	 */	
	public void addStructuralListener(StructuralListener listener) {
		if (destroyed) {
			throw new IllegalStateException("[" + this + "] destroyed");
		}
		childHelper.addStructuralListener(listener);
	}
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}
	
	/**
	 * Override toString.
	 */	
	public String toString() {
	    if (name == null) {
	        return getClass().getSimpleName();
	    }
	    else {
	        return name;
	    }
	}

	/**
	 * Return an icon tip for a given id. Part
	 * of the Iconic interface.
	 */
	public ImageData iconForId(String iconId) {
		return icon;
	}

	/**
	 * Add an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	public void addIconListener(IconListener listener) {
		if (destroyed) {
			throw new IllegalStateException("[" + this + "] destroyed");
		}
		listener.iconEvent(new IconEvent(this, "folder"));
	}

	/**
	 * Remove an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	public void removeIconListener(IconListener listener) {
	}

	
	public void initialised() {
	}
	
	public void configured() {
	}
		
	/**
	 * Destroy this component.
	 */
	public void destroy() {
		if (destroyed) {
			throw new IllegalStateException("[" + this + "] destroyed");
		}
		try {
			childHelper.stopChildren();
		} catch (FailedToStopException e) {
			logger.warn("Failed to stop.", e);
		}
		destroyed = true;
	}
	
}
