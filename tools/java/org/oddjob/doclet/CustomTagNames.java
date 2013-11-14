package org.oddjob.doclet;

/**
 * Constants for Oddjob's custom Javadoc tags used to build the 
 * Oddojob Reference.
 * 
 * @author rob
 *
 */
public interface CustomTagNames {

	public static final String EOL = System.getProperty("line.separator");
	
	/**
	 * Provide a description of the job or value.
	 */
	public static final String DESCRIPTION_TAG_NAME = "oddjob.description";
	
	public static final String DESCRIPTION_TAG = "@" + DESCRIPTION_TAG_NAME;
	
	/**
	 * Tag for a property of a job or value.
	 */
	public static final String PROPERTY_TAG_NAME = "oddjob.property";
	
	public static final String PROPERTY_TAG = "@" + PROPERTY_TAG_NAME;
	
	/**
	 * Tag for if the property is required.
	 */
	public static final String REQUIRED_TAG_NAME = "oddjob.required";
	
	public static final String REQUIRED_TAG = "@" + REQUIRED_TAG_NAME;
	
	/**
	 * Tag for an example of a Job or value.
	 */
	public static final String EXAMPLE_TAG_NAME = "oddjob.example";
	
	public static final String EXAMPLE_TAG = "@" + EXAMPLE_TAG_NAME;
	
	/**
	 * Tag for an XML resource that is to be loaded into the documentation
	 * as formatted XML.
	 */
	public static final String XML_RESOURCE_TAG_NAME = "oddjob.xml.resource";
	
	public static final String XML_RESOURCE_TAG = "@" + XML_RESOURCE_TAG_NAME;
	
	/**
	 * Tag for a Java Code file that is to be formatted and loaded
	 * into the documentation.
	 */
	public static final String JAVA_FILE_TAG_NAME = "oddjob.java.file";
	
	public static final String JAVA_FILE_TAG = "@" + JAVA_FILE_TAG_NAME;
	
	/**
	 * Tag for an XML file that is to be loaded into the documentation
	 * as formatted XML.
	 */
	public static final String XML_FILE_TAG_NAME = "oddjob.xml.file";
	
	public static final String XML_FILE_TAG = "@" + XML_FILE_TAG_NAME;
	
	
	/**
	 * Tag for a text file that is to be loaded into the documentation
	 * as formatted HTML.
	 */
	public static final String TEXT_FILE_TAG_NAME = "oddjob.text.file";
	
	public static final String TEXT_FILE_TAG = "@" + TEXT_FILE_TAG_NAME;
	
	
	/**
	 * Tag for a text resource that is to be loaded into the documentation
	 * as formatted HTML.
	 */
	public static final String TEXT_RESOURCE_TAG_NAME = "oddjob.text.resource";
	
	public static final String TEXT_RESOURCE_TAG = "@" + TEXT_RESOURCE_TAG_NAME;
	
}
