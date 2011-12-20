/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.doclet;

import org.oddjob.arooa.beandocs.BeanDoc;
import org.oddjob.arooa.beandocs.WriteableBeanDoc;
import org.oddjob.arooa.beandocs.WriteableExampleDoc;
import org.oddjob.arooa.beandocs.WriteablePropertyDoc;
import org.oddjob.tools.includes.CompositeLoader;
import org.oddjob.tools.includes.IncludeLoader;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

/**
 * A Processor is capable of processing a java ClassDoc object into
 * a reference PageData object.
 *   
 * @author Rob Gordon.
 */
public class Processor implements CustomTagNames {

	private final ClassDoc classDoc;
	private final String rootDir;
	private final JobsAndTypes jats;
	
	/**
	 * Create a processor.
	 *  
	 * @param jats The JobsAndTypes object to lookup up link names
	 * in.
	 * @param classDoc The classdoc the processor will process.
	 */
	public Processor(JobsAndTypes jats, ClassDoc classDoc) {
		this.jats = jats;
		this.classDoc = classDoc;
		String[] packages = classDoc.containingPackage().name().split(("[.]"));
		String rootDir = "";
		for (int i = 0; i < packages.length; ++i) {
			rootDir = rootDir + (i == 0 ? "" : "/") + "..";
		}
		this.rootDir = rootDir;
	}
	
	/**
	 * Process a single text tag from an array. This is for a property
	 * or element tag which is expected to be a single piece of text.
	 * Errors are produced if it's not. 
	 * 
	 * @param tags To process. There is expected to be either 0 or
	 * 1 tag. An error will be written to stderr if there is more.
	 * @param replacement The text to use if the tags are empty.
	 * 
	 * @return The text to use. Null if there was no tags.
	 */
	String processSingleTextTag(Tag[] tags, String replacement) {
        if (tags.length == 0) {
        	return null;
        }
        if (tags.length > 1) {
        	System.err.println("More than one tag for [" + replacement 
        			+ "]. Ignoring all others");
        }
        
        String text = tags[0].text();
        
        if (text == null || text.trim().equals("")) {
        	return replacement;
        }

        return text;
	}
	
    /**
     * Process fields and methods
     * 
     * @param beanDoc The job page this method will populate.
     * @param doc The field or method doc object.
     */
    void processFieldOrMethod(WriteableBeanDoc beanDoc, Doc doc) {
        Tag[] propertyTags = doc.tags(PROPERTY_TAG);
        
        String propertyText = processSingleTextTag(
        		propertyTags, doc.name());
        
        
        if (propertyText == null) {
        	// no property tag then ignore any other tags.
        	return;
        } 
        	
        WriteablePropertyDoc prop = beanDoc.propertyDocFor(propertyText);
        
        if (prop == null) {
        	System.err.println("No such property: " + propertyText);
        	return;
        }
        
        Tag[] descriptionTags = doc.tags(DESCRIPTION_TAG);
        prop.setAllText(processMajorTags(descriptionTags));
        prop.setFirstSentence(processFirstLine(
        		descriptionTags, this.rootDir));
        
        Tag[] rtags = doc.tags(REQUIRED_TAG);
        if (rtags.length != 0)  {
            prop.setRequired(rtags[0].text());
        }
    }
     
    /**
     * Process inline tags. Creates a link if appropriate.
     * 
     * @param inlines The inline tags.
     * @return The processes text.
     */
    String processInlineTags(Tag[] inlines, String rootDir) {
    	StringBuffer buffer = new StringBuffer();
    	for (int i = 0; i < inlines.length; ++i) {
    		Tag  inlineTag = inlines[i];
    		if (inlineTag instanceof SeeTag) {
    			SeeTag seeTag = (SeeTag) inlineTag;
    			String ref = seeTag.referencedClassName();
    			
    			String fileName = ref.replace('.', '/') +  ".html"; 
    			
    			BeanDoc beanDoc = jats.docFor(ref);
    			if (beanDoc != null) {
        			buffer.append("<a href='" + rootDir + "/" + fileName + "'>"
    						+ beanDoc.getName() + "</a>");
    			}
    			else if (ref.startsWith("org.oddjob")) {
        			buffer.append("<code><a href='" + getApiDirFrom(rootDir) + "/" + fileName + "'>"
    						+ ref + "</a></code>");    				
    			}
    			else {
    				buffer.append("<code>" + ref + "</code>");
    			}
    		}
    		else {
    			IncludeLoader loader = new CompositeLoader();
    			
    			if (loader.canLoad(inlineTag.name())) {
    				buffer.append(loader.load(inlineTag.text()));
    			}
    			else {
    				buffer.append(inlineTag.text());
    			}
    		}
    	}
    	
    	return buffer.toString();
    }
    
    /**
     * Process an array of tags. This is for a tag such as description.
     * 
     * @param tags An array of tags
     * @return The text equivalent.
     */
    String processMajorTags(Tag[] tags) {
    	if (tags == null) {
    		return null;
    	}
    	StringBuffer result = new StringBuffer();
        for (int i = 0; i < tags.length; ++i) {
            result.append(processInlineTags(
            		tags[i].inlineTags(),
            		this.rootDir));
        }
        return result.toString();
    }
    
    /**
     * Process the first line from a collection of major
     * tags such as description.
     * 
     * @param tags The major tags.
     * @return The first line text.
     */
    String processFirstLine(Tag[] tags, String rootDir) {
    	if (tags == null) {
    		return null;
    	}
    	if (tags.length == 0) {
    		return null;
    	}
    	return processInlineTags(tags[0].firstSentenceTags(), 
    			rootDir);
    }
    
    
    /**
     * Process all member fields and methods including superclasses.
     * 
     * @param pageData
     * @param classDoc
     */
    void processAllMembers(WriteableBeanDoc pageData, ClassDoc classDoc) {
        if (classDoc == null) {
        	return; 
        }
		processAllMembers(pageData, classDoc.superclass());
        
        FieldDoc[] fds = classDoc.fields();
        for (int i = 0; i < fds.length; ++i) {
            processFieldOrMethod(pageData, fds[i]);
        }
        MethodDoc[] mds = classDoc.methods();
        for (int i = 0; i < mds.length; ++i) {
            processFieldOrMethod(pageData, mds[i]);
        }
    	
    }
    
    /**
     * Create page data for a given doclet.
     * 
     * @param name
     * @param cd
     */
    public BeanDoc process() {
    	String fqcn = classDoc.qualifiedName();
    	
    	WriteableBeanDoc beanDoc = jats.docFor(fqcn);
        System.out.println("Processing: " + beanDoc.getName());
        
        Tag[] descriptionTags = classDoc.tags(DESCRIPTION_TAG);
        
        String firstLine = processFirstLine(descriptionTags, ".");
        if (firstLine == null) {
        	System.err.println("No oddjob.description tag for " + fqcn);
        }
        
        beanDoc.setFirstSentence(firstLine);
        beanDoc.setAllText(processMajorTags(descriptionTags));
        
        Tag[] exampleTags = classDoc.tags(EXAMPLE_TAG);
        for (int i = 0; i < exampleTags.length; ++i) {
        	
        	WriteableExampleDoc exampleDoc = new WriteableExampleDoc();
        	exampleDoc.setAllText(
            		processInlineTags(exampleTags[i].inlineTags(), 
            				this.rootDir));
        	exampleDoc.setFirstSentence(
        			processFirstLine(new Tag[] { exampleTags[i] },
        			this.rootDir));
        	beanDoc.addExampleDoc(exampleDoc);
        }

        processAllMembers(beanDoc, classDoc);
        return beanDoc;
    }

	public static String fqcnFor(ClassDoc classDoc) {
		return classDoc.containingPackage().name() + "." + classDoc.name();
	}
	
	private String getApiDirFrom(String rootDir) {
		return rootDir + "/../api";
	}
}
