package org.oddjob.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.registry.Path;
import org.oddjob.framework.Transient;

/**
 * @oddjob.description This provides common implementation for 
 * persisting job state.
 * 
 * @author Rob Gordon
 */

abstract public class PersisterBase 
implements OddjobPersister {

	private static final Logger logger = Logger.getLogger(PersisterBase.class);

	/** List of ids to include. */
	private List<String> include;

	/** List of ids to exclude. */
	private List<String> exclude;

	/**
	 * @oddjob.property path
	 * @oddjob.description A '/' delimited path to the location for the 
	 * persister. Normally this is set by nested persisters to be the id 
	 * of the Oddjob that created them.
	 * @oddjob.required No.
	 */
	private Path ourPath;
	
	/**
	 * Constructor.
	 */
	public PersisterBase() {
	}
	
	/**
	 * Constructor used for nested persisters.
	 * 
	 * @param path The path.
	 */
	protected PersisterBase(Path path) {
		this.ourPath = path;
	}	
	
	public void setPath(String path) {
		ourPath = new Path(path); 
	}
	
	public ComponentPersister persisterFor(String id) {
		Path path;
		if (ourPath == null) {
			path = new Path(id);
		}
		else {
			path = ourPath.addId(id);
		}
		
		logger.info("Created persister for id [" + id + "]");
		return new InnerPersister(path);
	}
		
	private class InnerPersister 
	implements OddjobPersister, ComponentPersister {
		
		private final Path path;
		
		private boolean closed;
		
		private List<InnerPersister> children = 
			new ArrayList<InnerPersister>();
		
		public InnerPersister(Path path) {
			this.path = path;
		}
		
		@Override
		public ComponentPersister persisterFor(String id) {
			if (id == null) {
				throw new NullPointerException("No path.");
			}
			InnerPersister child = new InnerPersister(
					this.path.addId(id)) {
				public void close() {
					super.close();
					children.remove(this);					
				}
			};
			children.add(child);
			return child;
		}

		@Override
		public void persist(String id, Object proxy, ArooaSession session) 
		throws ComponentPersistException {
			if (closed) {
				return;
			}
			
			if (!(proxy instanceof Serializable)) {
				logger.debug("[" + proxy + "] not Serializable - will not persist.");
				return;
			}
			if ((proxy instanceof Transient)) {
				logger.debug("[" + proxy + "] is Transient - will not persist.");
				return;
			}
			if (include != null && !include.contains(id)) {
				logger.debug("[" + proxy + "], id [" + id
						+ "] not in include list - will not persist.");
				return;
			}
			if (exclude != null && exclude.contains(id)) {
				logger.debug("[" + proxy + "], id [" + id
						+ "] in exclude list - will not persist.");
				return;
			}

			PersisterBase.this.persist(path, id, proxy);
		}
		
		@Override
		public Object restore(String id, ClassLoader classLoader, 
				ArooaSession session) 
		throws ComponentPersistException {
			if (closed) {
				return null;
			}
			
			return PersisterBase.this.restore(path, id, classLoader);
		}
		
		@Override
		public void remove(String id, ArooaSession session) 
		throws ComponentPersistException {
			if (closed) {
				return;
			}
			
			PersisterBase.this.remove(path, id);
		}
	
		@Override
		public String[] list() 
		throws ComponentPersistException {
			return PersisterBase.this.list(path);
		}
		
		@Override
		public void clear() throws ComponentPersistException {
			if (closed) {
				return;
			}
			
			List<ComponentPersister> copy = 
				new ArrayList<ComponentPersister>(children);
			for (ComponentPersister child : copy) {
				child.clear();
			}
			
			logger.debug("Clearing persister for path [" + path + "]");
			PersisterBase.this.clear(path);
		}

		@Override
		public void close() {
			closed = true;
		}
		
		@Override
		public String toString() {
			return PersisterBase.this.toString() + ", path=" + path;
		}
	}
	
	/**
	 * Provided by subclasses to do the persisting.
	 * 
	 * @param path The path as a string. Never null.
	 * @param id The id. Never Null.
	 * @param component The component or it's proxy.
	 */
	abstract protected void persist(Path path, String id, Object component)
	throws ComponentPersistException;
	
	/**
	 * Restore a previously persisted Component or it's Proxy.
	 * 
	 * @param path The path. Never Null.
	 * @param id The id. Never Null.
	 * @param classLoader The classLoader.
	 * 
	 * @return The component or it's proxy. Null if nothing had previously been
	 * persisted for this path and id.
	 */
	abstract protected Object restore(Path path, String id, ClassLoader classLoader)
	throws ComponentPersistException;
	
	abstract protected String[] list(Path path)
	throws ComponentPersistException;
	
	/**
	 * Remove a possibly previously persisted object.
	 * 
	 * @param path The path.
	 * @param id The id.
	 */
	abstract protected void remove(Path path, String id)
	throws ComponentPersistException;
	
	/**
	 * Remove a possibly previously persisted object.
	 * 
	 * @param path The path.
	 * @param id The id.
	 */
	abstract protected void clear(Path path)
	throws ComponentPersistException ;
}
