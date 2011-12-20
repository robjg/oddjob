package org.oddjob.tools.doclet.utils;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

/**
 * Process see and link tags.
 * 
 * @author rob
 *
 */
public class SeeTagProcessor implements TagProcessor {
	
	@Override
	public String process(Tag tag) {
		
		if (! (tag instanceof SeeTag)) {
			return null;
		}
		
		SeeTag seeTag = (SeeTag) tag;
		
		ClassDoc referencedClassDoc = seeTag.referencedClass();		
		String simpleClassName =  referencedClassDoc.name();
				
		String referencedClassName = seeTag.referencedClassName();
		
		String fileName = referencedClassName.replace('.', '/') +  ".html"; 
		
		String rootDir = new ClassDocUtils(seeTag.holder()).getRelativeRootDir();
		
		return("<code><a href='" + rootDir + "/" + fileName + "'>"
					+ simpleClassName + "</a></code>");    				
	}	
}
