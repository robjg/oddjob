package org.oddjob.persist;

import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.registry.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Memory based {@link OddjobPersister}.
 * 
 * @author rob
 *
 */
public class MapPersister extends PersisterBase {
	private static final Logger logger = LoggerFactory.getLogger(
			MapPersister.class);
	
	private final Map<Path, Map<String, byte[]>> cache;
	
	/**
	 * Create a new instance with a standard {@link HashMap}.
	 */
	public MapPersister() {
		this(new HashMap<Path, Map<String, byte[]>>());
	}
	
	/**
	 * Create a new instance with the provided map. Access to this
	 * map will be sychronized so the map itself need not be. 
	 * 
	 * @param store A Map. Must not be null.
	 */
	public MapPersister(Map<Path, Map<String, byte[]>> store) {
		if (store == null) {
			throw new NullPointerException("No Map!");
		}
		this.cache = store;
	}
	
	@Override
	protected void persist(Path path, String id, Object proxy) {
		logger.info("Saving [" + path + "], [" + id + "]");
		
		synchronized (cache) {
			Map<String, byte[]> inner = cache.get(path);
			if (inner == null) {
				inner = new TreeMap<String, byte[]>();
				cache.put(path, inner);
			}
			inner.put(id, new SerializeWithBytes().toBytes(proxy));
		}
	}
	
	@Override
	protected Object restore(Path path, String id, ClassLoader classLoader) {
		
		byte[] buffer;;
		synchronized (cache) {
			Map<String, byte[]> inner = cache.get(path);
			if (inner == null) {
				logger.info("Restore Failed. No cache for path [" + path + "],[" + id + "]");
				return null;
			}
			buffer = inner.get(id);
		}
		
		if (buffer == null) {
			logger.info("Restore Failed. Nothing saved for [" + path + "], [" + id + "]");
			return null;
		}
		
		logger.info("Restoring [" + path + "], [" + id + "]");
		
		return new SerializeWithBytes().fromBytes(buffer, classLoader);
	}
	
	@Override
	protected String[] list(Path path)
			throws ComponentPersistException {
		synchronized (cache) {
			Map<String, byte[]> inner = cache.get(path);
			if (inner == null) {
				return null;
			}
			return inner.keySet().toArray(new String[inner.size()]);
		}
	}
	
	@Override
	protected void remove(Path path, String id) {
		logger.info("Removing " + path + ", " + id);
		synchronized (cache) {
			Map<String, byte[]> inner = cache.get(path);
			if (inner == null) {
				return;
			}
			inner.remove(id);
		}
	}
	
	@Override
	protected void clear(Path path) {
		logger.info("Clearing " + path);
		synchronized (cache) {
			cache.remove(path);
		}
	}
	
	@Override
	public String toString() {
		int size;
		synchronized (cache) {
			size = cache.size();
		}
		return getClass().getSimpleName() + ", " + size + " saved";
	}
}
