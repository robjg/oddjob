package org.oddjob.tools.doclet.utils;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;

/**
 * Helper methods for working with {@link ClassDoc}.
 * 
 * @author rob
 *
 */
public class ClassDocUtils {

	private final ClassDoc classDoc;
	
	/**
	 * Constructor that finds the ClassDoc from other Docs.
	 * 
	 * @param doc
	 */
	public ClassDocUtils(Doc doc) {
		if (doc instanceof ClassDoc) {
			classDoc = (ClassDoc) doc;
		}
		else if (doc instanceof MethodDoc) {
			classDoc = ((MethodDoc) doc).containingClass();
		}
		else if (doc instanceof FieldDoc) {
			classDoc = ((FieldDoc) doc).containingClass();
		}
		else {
			throw new IllegalArgumentException(
					"Can't support " + doc.getClass() + " yet");
		}
	}
	
	/**
	 * Get the path of the root director.
	 * 
	 * @return The path. Never null.
	 */
	public String getRelativeRootDir() {
		String[] packages = classDoc.containingPackage().name().split(("[.]"));
		String rootDir = "";
		for (int i = 0; i < packages.length; ++i) {
			rootDir = rootDir + (i == 0 ? "" : "/") + "..";
		}
		
		return rootDir;
	}
}
