package org.oddjob.tools.includes;

import java.io.File;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.doclet.CustomTagNames;

/**
 * Creates text that can be inserted into JavaDoc or another XML document from
 * a Java Source Code File.
 * 
 * 
 * @author rob
 *
 */
public class JavaCodeFileLoader implements IncludeLoader, CustomTagNames {
	
	private static final Logger logger = LoggerFactory.getLogger(JavaCodeFileLoader.class);
	
	private final File base;
	
	public JavaCodeFileLoader(File base) {
		this.base = base;
	}
		
	@Override
	public boolean canLoad(String tag) {
		return JAVA_FILE_TAG.equals(tag);
	}
	
	public String load(String path) {
		
		try {			
			FilterFactory filterFactory = new FilterFactory(path);
			
			File file = new File(base, filterFactory.getResourcePath());
			
			logger.info("Reading file " + file);
			
			String result = filterFactory.getTextLoader().load(
					new FileInputStream(file));
			
			Java2HTML java2html = new Java2HTML();

			return java2html.convert(result);
		}
		catch (Exception e) {
			return "<p><em>" + e.toString() + "</em></p>" + EOL;
		}
	}
}
