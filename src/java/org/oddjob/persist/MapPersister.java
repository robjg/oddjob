package org.oddjob.persist;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.registry.Path;

public class MapPersister extends PersisterBase {
	private static final Logger logger = Logger.getLogger(
			MapPersister.class);
	
	private final Map<Path, Map<String, byte[]>> cache;
	
	public MapPersister() {
		this(new HashMap<Path, Map<String, byte[]>>());
	}
	
	public MapPersister(Map<Path, Map<String, byte[]>> store) {
		this.cache = store;
	}
	
	@Override
	protected void persist(Path path, String id, Object proxy) {
		logger.info("Saving [" + path + "], [" + id + "]");
		
		Map<String, byte[]> inner = cache.get(path);
		if (inner == null) {
			inner = new TreeMap<String, byte[]>();
			cache.put(path, inner);
		}
		inner.put(id, new SerializeWithBytes().toBytes(proxy));
	}
	
	@Override
	protected Object restore(Path path, String id, ClassLoader classLoader) {
		logger.info("Restoring [" + path + "], [" + id + "]");
		Map<String, byte[]> inner = cache.get(path);
		if (inner == null) {
			return null;
		}
		byte[] buffer = inner.get(id);
		if (buffer == null) {
			return null;
		}
		
		return new SerializeWithBytes().fromBytes(buffer, classLoader);
	}
	
	@Override
	protected String[] list(Path path)
			throws ComponentPersistException {
		Map<String, byte[]> inner = cache.get(path);
		if (inner == null) {
			return null;
		}
		return inner.keySet().toArray(new String[inner.size()]);
	}
	
	@Override
	protected void remove(Path path, String id) {
		logger.info("Removing " + path + ", " + id);
		Map<String, byte[]> inner = cache.get(path);
		if (inner == null) {
			return;
		}
		inner.remove(id);
	}
	
	@Override
	protected void clear(Path path) {
		logger.info("Clearing " + path);
		cache.remove(path);
	}
}
