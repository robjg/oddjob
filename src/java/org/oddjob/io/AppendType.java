/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

/**
 * @oddjob.description Specify a file for appending to.
 * <p>
 * 
 * @oddjob.example
 * 
 * Append a line to a file.
 * 
 * {@oddjob.xml.resource org/oddjob/io/AppendExample.xml}
 * 
 * 
 * @author Rob Gordon.
 */
public class AppendType implements ArooaValue {

	public static class Conversions implements ConversionProvider {

		public void registerWith(ConversionRegistry registry) {
//	    	registry.register(AppendType.class, File.class, 
//	    			new Convertlet<AppendType, File>() {
//	    		public File convert(AppendType from) throws ConvertletException {
//	    	    	return from.file;
//	    		}
//	    	});
//	    	
//	    	registry.register(AppendType.class, File[].class, 
//	    			new Convertlet<AppendType, File[]>() {
//	    		public File[] convert(AppendType from) throws ConvertletException {
//	    	    	return new File[] { from.file };
//	    		}
//	    	});
	    	
	    	registry.register(AppendType.class, OutputStream.class, 
	    			new Convertlet<AppendType, OutputStream>() {
	    		public OutputStream convert(AppendType from) throws ConvertletException {
	    	    	try {
						return new BufferedOutputStream(new FileOutputStream(
								from.file, true));
					} catch (FileNotFoundException e) {
						throw new ConvertletException(e);
					}
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
    public void setFile(File file) {
        this.file = file;
    }
   
    public String toString() {
    	return "Apppend " + file.toString();
    }

}
