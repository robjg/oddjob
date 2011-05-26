package org.oddjob.tools.includes;

public interface IncludeLoader {

	public boolean canLoad(String tag);
	
	public String load(String path);
}
