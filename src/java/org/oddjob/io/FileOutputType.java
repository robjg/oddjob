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
public class FileOutputType implements ArooaValue {

	public static class Conversions implements ConversionProvider {

		public void registerWith(ConversionRegistry registry) {
	    	registry.register(FileOutputType.class, File.class, 
	    			new Convertlet<FileOutputType, File>() {
	    		public File convert(FileOutputType from) throws ConvertletException {
	    	    	return from.file;
	    		}
	    	});
	    	
	    	registry.register(FileOutputType.class, File[].class, 
	    			new Convertlet<FileOutputType, File[]>() {
	    		public File[] convert(FileOutputType from) throws ConvertletException {
	    	    	return new File[] { from.file };
	    		}
	    	});
	    	
	    	registry.register(FileOutputType.class, OutputStream.class, 
	    			new Convertlet<FileOutputType, OutputStream>() {
	    		public OutputStream convert(FileOutputType from) throws ConvertletException {
	    	    	try {
						return new BufferedOutputStream(new FileOutputStream(
								from.file, from.append));
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
        
    /**
     * @oddjob.property
     * @oddjob.description When used as output, append to a file if true,
     * create a new file if false.
     * @oddjob.required No. Defaults to false.
     */
    private boolean append;

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
   
	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}
	
    public String toString() {
    	return "File" + file.toString();
    }

}
