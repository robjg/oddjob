/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryCrawler;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.Path;

public class JobToken implements Serializable {
	private static final long serialVersionUID = 20060112;
	
	private final transient Object job;
	private final String path;

	private JobToken(String path, Object job) {
		this.path = path;
		this.job = job;
	}
	
	/**
	 * Create a job token for the given job using the given registry.
	 * 
	 * @param registry The component registry.
	 * @param job The job.
	 * @return
	 */
	public static JobToken create(BeanRegistry registry, Object job) {
		if (job == null) {
			throw new NullPointerException("No Job!");
		}
		if (registry == null) {
			return new JobToken(null, job);
		}
		BeanDirectoryCrawler crawler = new BeanDirectoryCrawler(registry);
		Path path = crawler.pathForObject(job);
		if (path == null) {
			throw new NullPointerException("No path for [" + job + "]");
		}
		
		return new JobToken(path.toString(), job);
	}

	/**
	 * Retrieve a job from a ComponentRegistry from it's token.
	 * 
	 * @param registry The component registry.
	 * @param token The job token.
	 * 
	 * @return The actual job.
	 */
	public static Object retrieve(BeanDirectory registry, JobToken token) 
	throws ArooaPropertyException {
		if (token.path != null) {
			return registry.lookup(token.path);
		}
		return token.job;
	}
	
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		if (path == null) {
			throw new NotSerializableException("Can't serialize - has the job got an id?");
		}
		s.defaultWriteObject();
	}

	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
	}	
	
	public String toString() {
		if (path != null) {
			return "Path: " + path;
		}
		else {
			return job.toString();
		}
	}
}
