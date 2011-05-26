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
 * <pre>
 * &lt;files files="onefile.txt" /&gt;
 * </pre>
 * 
 * @oddjob.example
 * 
 * Using a wildcard expression.
 * 
 * <pre>
 * &lt;files files="reports/*.txt" /&gt;
 * </pre>
 * 
 * @oddjob.example
 * 
 * Specifying a list of files.
 * 
 * <pre>
 * &lt;files&gt;
 *  &lt;list&gt;
 *   &lt;files files="onefile.txt" /&gt;
 *   &lt;files files="reports/*.txt" /&gt;
 *  &lt;/list&gt;
 * &lt;/files&gt;
 * </pre>
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
	    	
	    	registry.register(FilesType.class, File.class, 
	    			new Convertlet<FilesType, File>() {
	    		public File convert(FilesType from) throws ConvertletException {
	    			File[] results = from.toFiles();
	    			if (results.length == 0)  {
	    				return null;
	    			}
	    			return results[0];
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
    		addFileArray(all, Files.expand(
    			new File[] { new File(files) }));
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
