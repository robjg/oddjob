package org.oddjob.jobs;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;


/**
 * @oddjob.description Provide a sequence number which is 
 * incremented each time the job is executed.
 *
 * @oddjob.example
 * 
 * Using a sequence in a file name.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/SequenceInFileNameExample.xml}
 * 
 * The watch property is set to watch when the daily scheule move forward
 * so the sequence is restarted.
 * 
 * 
 * @author rob
 */
public class SequenceJob implements Runnable, Serializable {
	private static final long serialVersionUID=20060109;

	private static final Logger logger = LoggerFactory.getLogger(SequenceJob.class);
	
    /**
     * @oddjob.property
     * @oddjob.description The name of this job.
     * @oddjob.required No.
     */
	private transient String name;
	
    /**
     * @oddjob.property 
     * @oddjob.description The current sequence number.
     * @oddjob.required Set automatically, but can be changed manually.
     */
	private volatile Integer current;
	
    /**
     * @oddjob.property 
     * @oddjob.description The sequence number to start from.
     * @oddjob.required No, defaults to 0.
     */
	private int from;
	
	/**
	 * @oddjob.property
	 * @oddjob.description This can be any object which
	 * will be watched, and when it changes the sequence
	 * will be reset. This will most likely be a date.
	 * @oddjob.required. No.
	 */
	private transient Object watch;
	
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
	 * Get the current sequence number.
	 * 
	 * @return The current sequence number.
	 */
	public Integer getCurrent() {
		return current;
	}

	public void setCurrent(Integer current) {
		logger.info("Current value set to " + current + " via property.");
		this.current = current;
	}
	
	public void setFrom(int from) {
		this.from = from;
	}
	
	public int getFrom() {
		return from;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public synchronized void run() {
		if (current == null) {
			current = new Integer(from);
		}
		else {
			current = new Integer(current.intValue() + 1);;
		}
		logger.info("Sequence now " + current);
	}
	
	
	/**
	 * Set an object to watch.
	 * 
	 * @param reset The reset to set.
	 */
	@ArooaAttribute
	public void setWatch(Object watch) {
		if (this.watch == null) {
			if (watch == null) {
				return;
			}
			else {
				current = null;
			}
		}
		else if (!this.watch.equals(watch)) {
			current = null;
		}
		this.watch = watch;
	}
	
	public Object getWatch() {
		return watch;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return "A Sequence Number"; 
		}
		return name;
	}
}
