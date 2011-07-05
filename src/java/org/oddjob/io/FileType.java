/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.File;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;

/**
 * @oddjob.description Specify a file. In addition to being useful for
 * configuring a job property that requires a file, this type can be used 
 * wherever an input or output is required.
 * <p>
 * 
 * @oddjob.example
 * 
 * Set the {@link CopyJob} file copy example.
 * 
 * 
 * @author Rob Gordon.
 */
public class FileType implements ArooaValue {

	public static class Conversions implements ConversionProvider {

		public void registerWith(ConversionRegistry registry) {
	    	registry.register(FileType.class, File.class, 
	    			new Convertlet<FileType, File>() {
	    		public File convert(FileType from) throws ConvertletException {
	    	    	return from.file;
	    		}
	    	});
	    	
	    	registry.register(FileType.class, File[].class, 
	    			new Convertlet<FileType, File[]>() {
	    		public File[] convert(FileType from) throws ConvertletException {
	    	    	return new File[] { from.file };
	    		}
	    	});
		}
	}
	
    /**
     * @oddjob.property
     * @oddjob.description The file path.
     * @oddjob.required Yes.
     */
    private File file;
        
	public File getFile() {
		return file;
	}

    /**
     * Set the file.
     * 
     * @param file The file.
     */
    @ArooaAttribute
    public void setFile(File file) {
        this.file = file;
    }
   
    public String toString() {
    	return "File " + file.toString();
    }

}
