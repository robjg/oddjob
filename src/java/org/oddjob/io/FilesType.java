/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;

/**
 * @oddjob.description Specify files using a wild card pattern, or a
 * a list. The list can contain {@link FileType} or other types that 
 * can be converted into a java File object or array including this type. 
 * In this way this type can be used to build complicated collections of 
 * files.
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

	public static int A_FEW = 5;
	
	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
	    	registry.register(FilesType.class, File[].class, 
	    			new Convertlet<FilesType, File[]>() {
	    		public File[] convert(FilesType from) throws ConvertletException {
	    			try {
	    				return from.toFiles();
					} catch (IOException e) {
						throw new ConvertletException(e);
					}
	    		}
	    	});
		}
	}
	
    /**
     * @oddjob.property 
     * @oddjob.description The files
     * @oddjob.required No
     */
    private volatile String files;
    
    /**
     * @oddjob.property 
     * @oddjob.description More files
     * @oddjob.required No
     */
    private final List<File[]> list = new CopyOnWriteArrayList<>();

    /** Remember last conversion for to string. */
    private volatile File[] lastConversion; 
    
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
    
    public File[] toFiles() throws IOException {
    	
    	List<File> all = new ArrayList<File>();
    	
    	if (files != null) {
    		addFileArray(all, Files.expand(new File(files)));
    	}
    	
    	for (File[] files : list) {
    		addFileArray(all, files);
    	}
    	
    	this.lastConversion = all.toArray(new File[all.size()]);
    	
    	return this.lastConversion;
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
    	
    	StringBuilder text = new StringBuilder();
    	text.append("Files: ");

    	File[] last = this.lastConversion;
    	if (last == null) {
    		String files = this.files;
        	if (files != null) {
        		text.append(files);
        	}
        	int size = list.size();
        	if (size > 0) {
        		if (files != null) {
        			text.append(" and a ");
        		}
        		text.append("list of size ");
        		text.append(size);
        	}
        	if (files == null && size == 0) {
        		text.append(", none yet specified");
        	}
    	}
    	else {
    		File[] aFew;
    		if (last.length > A_FEW) {
    			aFew = new File[A_FEW];
    			System.arraycopy(last, 0, aFew, 0, A_FEW);
    		}
    		else {
    			aFew = last;
    		}

			text.append(new FileConvertlets().filesToPath(aFew));
			
    		if (aFew != last) {
    			text.append(" and ");
    			text.append(last.length - A_FEW);
    			text.append(" more");    			
    		}
    	}
    	
    	return text.toString();
    }
    
}
