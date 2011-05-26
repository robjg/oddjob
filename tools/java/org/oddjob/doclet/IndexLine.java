package org.oddjob.doclet;

public class IndexLine {

	private final String className;
	private final String name;
	private final String firstSentence;
	
	public IndexLine(String className, String name, String firstLine) {
		this.className = className;
		this.name = name;
		this.firstSentence = firstLine;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFirstSentence() {
		return firstSentence;
	}
}
