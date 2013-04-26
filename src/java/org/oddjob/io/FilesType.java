/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;

/**
 * @oddjob.description Specify files using a wildcard pattern. Also support
 * building complicated collections of files using the list property.
 * 
 * @oddjob.example
 * 
 * A single file.
 * 
 * {@oddjob.xml.resource org/oddjob/io/FilesTypeSimple1.xml}
 * 
 * @oddjob.example
 * 
 * Using a wildcard expression.
 * 
 * {@oddjob.xml.resource org/oddjob/io/FilesTypeSimple2.xml}
 * 
 * @oddjob.example
 * 
 * Specifying a list of files.
 * 
 * {@oddjob.xml.resource org/oddjob/io/FilesTypeSimple3.xml}
 * 
 * @oddjob.example
 * 
 * A complex version of building up a file list. It includes taking 
 * advantage of Oddjob's built in path conversion and adds in files
 * specified as arguments passed in to Oddjob.
 * 
 * {@oddjob.xml.resource org/oddjob/io/FilesTypeMixedList.xml}
 * 
 * 
 * @author Rob Gordon.
 */
public class FilesType implements ArooaValue, Serializable {
	private static final long serialVersionUID = 2009072300L;
	
	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
	    	registry.register(FilesType.class, File[].class, 
	    			new Convertlet<FilesType, File[]>() {
	    		public File[] convert(FilesType from) throws ConvertletException {
	    			return from.toFiles();
	    		}
	    	});
		}
	}
	
    /**
     * @oddjob.property 
     * @oddjob.description The files
     * @oddjob.required No
     */
    private String files;
    
    /**
     * @oddjob.property 
     * @oddjob.description More files
     * @oddjob.required No
     */
    private final List<File[]> list = new ArrayList<File[]>();
    
    /**
     * Set the directory for a scan.
     * 
     * @param directory The directory. 
     */
    public void setFiles(String files) {
        this.files = files;
    }
    
    public String getFiles() {
    	return files;
    }
    
    public void setList(int index, File[] files) {
    	if (files == null) {
    		list.remove(index);
    	}
    	else {
    		list.add(index, files);
    	}
    }
    
    public File[] toFiles() {
    	
    	List<File> all = new ArrayList<File>();
    	
    	if (files != null) {
    		addFileArray(all, Files.expand(new File(files)));
    	}
    	
    	for (File[] files : list) {
    		addFileArray(all, files);
    	}
    	
    	return all.toArray(new File[all.size()]);
    }
    
    private void addFileArray(List<File> list, File[] array) {
    	
    	for (File file : array) {
    		if (list.contains(file)) {
    			continue;
    		}
    		list.add(file);
    	}
    }
    
    public String toString() {
    	return "Files";
    }
    
}
