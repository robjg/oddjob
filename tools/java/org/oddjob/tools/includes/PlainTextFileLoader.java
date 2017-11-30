package org.oddjob.tools.includes;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.doclet.CustomTagNames;

/**
 * Creates Text that can be inserted into JavaDoc or another HTML document 
 * from a plain text file.
 * 
 * @author rob
 *
 */
public class PlainTextFileLoader implements IncludeLoader, CustomTagNames {

	private static final Logger logger = LoggerFactory.getLogger(PlainTextFileLoader.class);
	
	private final File base;
	
	public PlainTextFileLoader(File base) {
		this.base = base;
	}
	
	@Override
	public boolean canLoad(String tag) {
		return TEXT_FILE_TAG.equals(tag);
	}
	
	@Override
	public String load(String fileName) {
		
		try {
			File file = new File(base, fileName);
			
			logger.info("Reading file " + file);
			
			InputStream input = new FileInputStream(file);
			
			return new PlainTextToHTML().toHTML(input);
		}
		catch (Exception e) {
			return "<p><em>" + e.toString() + "</em></p>" + EOL;
		}
	}
}
