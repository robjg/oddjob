/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class used to provide a summary of of a scheduled job.
 */
public class ScheduleSummary implements Serializable {
	private static final long serialVersionUID = 20051117;

	/** The id the job is scheduled with. */
	private String id;
	
	/** A free form description of the jobs properties. */ 
	private Map<String, String> description = 
		new LinkedHashMap<String, String>();

	/**
	 * Get the Id.
	 * 
	 * @return The id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id.
	 * 
	 * @param id The id.
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Get the description.
	 * 
	 * @return The description;
	 */
	public Map<String, String> getDescription() {
		return description;
	}

	/**
	 * Set the description.
	 * 
	 * @param description The description.
	 */
	public void setDescription(Map<String, String> description) {
		this.description = description;
	}
}
