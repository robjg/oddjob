package org.oddjob.tools.includes;

import java.util.HashMap;
import java.util.Map;

/**
 * As yet unused.
 * 
 * @author rob
 *
 */
public class CompositeLoader implements IncludeLoader {

	private Map<String, IncludeLoader> loaders = 
		new HashMap<String, IncludeLoader>();
	
	private IncludeLoader selected;
	
	public CompositeLoader() {
		loaders.put(JavaCodeResourceLoader.TAG, new JavaCodeResourceLoader());
		loaders.put(XMLResourceLoader.XML_RESOURCE_TAG, new XMLResourceLoader());
	}
		
	@Override
	public boolean canLoad(String tag) {
		selected = loaders.get(tag);
		return selected != null;
	}
	
	@Override
	public String load(String path) {
		if (selected == null) {
			throw new IllegalStateException("Check canLoad first.");
		}
		return selected.load(path);		
	}
}
