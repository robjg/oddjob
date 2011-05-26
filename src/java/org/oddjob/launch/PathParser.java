package org.oddjob.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathParser {

	private String[] elements;
	
	public String[] getElements() {
		return elements;
	}
	
	public String[] processArgs(String[] args) {
		
		List<String> returned = new ArrayList<String>();
		
		String classpath = null;
		for (int i = 0; i < args.length; ++i) {
			if ("-cp".equals(args[i]) || "-classpath".equals(args[i])) {
				classpath = args[++i];
			}
			else {
				returned.add(args[i]);
			}
		}
	
		if (classpath != null) {
			elements = classpath.split(File.pathSeparator);
		}
		else {
			elements = new String[0];
		}
		
		return returned.toArray(new String[0]);
	}
	
}
