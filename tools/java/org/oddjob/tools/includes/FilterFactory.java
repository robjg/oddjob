package org.oddjob.tools.includes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterFactory {

	public final static Pattern PATTERN = Pattern.compile("([^#]+)(?:#(.*))?");
	
	private final StreamToText textLoader;
	
	private final String resourcePath;
	
	public FilterFactory(String path) {
		
		Matcher matcher = PATTERN.matcher(path);
		
		if (!matcher.matches()) {
			throw new IllegalArgumentException(
					"Invalid Include Path: " + path);
		}
		
		this.resourcePath = matcher.group(1);
		
		String snippet = matcher.group(2);
		
		if (snippet == null) {
			textLoader = new PlainStreamToText();
		}
		else {
			textLoader = new SnippetFilter(snippet);
		}
	}
	
	public StreamToText getTextLoader() {
		return textLoader;
	}
	
	public String getResourcePath() {
		return resourcePath;
	}
}
