package org.oddjob.tools.includes;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.io.ResourceType;

/**
 * Creates text that can be inserted into JavaDoc or another XML document from
 * a Java Source Code File.
 * 
 * 
 * @author rob
 *
 */
public class JavaCodeResourceLoader implements IncludeLoader {
	
	private static final Logger logger = LoggerFactory.getLogger(JavaCodeResourceLoader.class);
	
	private static final String EOL = System.getProperty("line.separator");
	
	public static final String TAG = "@oddjob.java.resource";
				
	@Override
	public boolean canLoad(String tag) {
		return TAG.equals(tag);
	}
	
	public String load(String path) {
		
		try {			
			FilterFactory filterFactory = new FilterFactory(path);
			
			String resource = filterFactory.getResourcePath();
			
			InputStream input = new ResourceType(
					resource).toInputStream();
			
			logger.info("Reading resource " + resource);
			
			String result = filterFactory.getTextLoader().load(
					input);
			
			Java2HTML java2html = new Java2HTML();

			return java2html.convert(result);
		}
		catch (Exception e) {
			return "<p><em>" + e.toString() + "</em></p>" + EOL;
		}
	}
}
