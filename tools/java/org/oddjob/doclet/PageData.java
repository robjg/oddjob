/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.doclet;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.parsing.QTag;


/**
 * Collect all the data required to write a manual page together in one
 * place.
 * 
 * @author Rob Gordon.
 */
public class PageData {

	/** The tag name */
    private final QTag name;
    
    /** The relative path to the directory where
     * this page should be written.
     */
    private final String fileName;
    
    private String firstSentence;
    
    /** The description */
    private String description;
    
    /** Examples as Strings */
    private final List<String> examples = 
    	new ArrayList<String>();
    
    /** Attributes as Property objects */
    private final List<Property> attributes = 
    	new ArrayList<Property>();
    
    /** Elements as Property objects */
    private final List<Property> elements = 
    	new ArrayList<Property>();
    
    /**
     * Constructor.
     * 
     * @param name The tag name.
     */
    public PageData(QTag name, String filename) {
        this.name = name;
        this.fileName = filename;
    }

    /**
     * Get the short (xml tag) name for this page data.
     *  
     * @return The short name.
     */
    public QTag getName() {
        return name;
    }
    
    public void setFirstSentence(String firstSentence) {
        this.firstSentence = firstSentence;
    }
    
    public String getFirstSentence() {
        return firstSentence;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void addAttribute(Property attribute) {
        attributes.add(attribute);
    }
    
    public List<Property> getAttributes() {
        return attributes;
    }
    
    public void addElement(Property element) {
        elements.add(element);
    }
    
    public List<Property> getElements() {
        return elements;
    }
    
    public void addExample(String example) {
        examples.add(example);
    }
    
    public List<String> getExamples() {
        return examples;
    }
	/**
	 * @return Returns the file name.
	 */
	public String getFileName() {
		return fileName;
	}
}

