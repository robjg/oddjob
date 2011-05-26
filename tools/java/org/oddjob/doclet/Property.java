/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.doclet;

/**
 * Encapsulate the data about a property or element that are
 * derrived from either field or method javadoc.
 *
 * @author Rob Gordon.
 */
public class Property {

	/** The name of the property as appears in xml. */
    private final String name;
    
    /** The descritpion. */
    private String description;
    
    /** The required text. */
    private String required;
    
    /**
     * Constructor.
     * 
     * @param name The name of the property or element.
     */
    public Property(String name) {
    	if (name == null) {
    		throw new IllegalArgumentException("name can never be null");
    	}
        this.name = name;
    }

    /**
     * Get the name of this property or element.
     * 
     * @return The name. Never null.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the description.
     * 
     * @param description The description text. May be null.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the description.
     * 
     * @return The description text. May be null.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the required text.
     * 
     * @param required The required text. May be null.
     */
    public void setRequired(String required) {
        this.required = required;
    }
    
    /**
     * Get the required text.
     * 
     * @return The required text. May be null.
     */
    public String getRequired() {
        return required;
    }
}

