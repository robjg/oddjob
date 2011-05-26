/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.doclet;

import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.beandocs.SessionArooaDocFactory;
import org.oddjob.arooa.beandocs.WriteableArooaDoc;
import org.oddjob.arooa.deploy.ClassPathDescriptorFactory;
import org.oddjob.arooa.standard.StandardArooaSession;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

/**
 *
 * @author Rob Gordon.
 */
public class ManualDoclet {

	private final JobsAndTypes jats;
	private final Archiver archiver;

	public ManualDoclet(String resource) {		
		ClassPathDescriptorFactory factory = 
			new ClassPathDescriptorFactory();

		ArooaDescriptor descriptor = factory.createDescriptor(
				getClass().getClassLoader());
		
    	ArooaSession session = new StandardArooaSession(descriptor);
		
    	SessionArooaDocFactory docsFactory = new SessionArooaDocFactory(session);
    	
    	WriteableArooaDoc jobs = 
    		docsFactory.createBeanDocs(ArooaType.COMPONENT);
    	
    	WriteableArooaDoc types = 
    		docsFactory.createBeanDocs(ArooaType.VALUE);
    	
    	this.jats = new JobsAndTypes(jobs, types);
    	this.archiver = new Archiver(jats);
	}
	
    void process(ClassDoc cd) {
    	archiver.archive(cd);
    }
    
    void process(RootDoc rootDoc, String destination) {
        ClassDoc[] cd = rootDoc.classes();
        
        System.out.println("ManualDoc: Working through " + 
        		cd.length + " classes.");
        
        for (int i = 0; i < cd.length; ++i) {
            process(cd[i]);
        }
        ManualWriter w = new ManualWriter(destination);
        w.createManual(archiver);
    }
    
    public static boolean start(RootDoc rootDoc) {
    	
    	System.out.println("Starting ManualDoclet.");
    	
		ClassLoader loader = ManualDoclet.class.getClassLoader();
		
    	System.out.println("ClassLoader stack:");
    	for (ClassLoader next = loader; next != null; next = next.getParent()) {
    		System.out.println("  " + next);
    	}    			
		
    	Oddjob test = new Oddjob();
    	System.out.println(test);
    	
    	String destination = readOptions(rootDoc.options());
    			
    	// need to work this out.
    	String resource = null;
    	
    	ManualDoclet md = new ManualDoclet(resource);
        md.process(rootDoc, destination);
        return true;
    }
    
    private static String readOptions(String[][] options) {
        String destination = null;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals("-d")) {
            	destination = opt[1];
            }
        }
        return destination;
    }

    public static int optionLength(String option) {
    	if (option.equals("-d")) {
    		return 2;
    	}
    	if (option.equals("-r")) {
    		return 2;
    	}
    	return 0;
    }
    public static boolean validOptions(String options[][], 
		       DocErrorReporter reporter) {
    	
    	boolean foundTagOption = false;

    	for (int i = 0; i < options.length; i++) {
    		String[] opt = options[i];
    		if (opt[0].equals("-d")) {
    			if (foundTagOption) {
    				reporter.printError("Only one -tag option allowed.");
    				return false;
    			} 
    			else { 
    				foundTagOption = true;
    			}
    		} 
    	}
    	if (!foundTagOption) {
    		reporter.printError("Usage: javadoc -d destinationDir" +
    				" [-r arooa-descriptor]" +
    				" -doclet ManualDoclet ...");
    	}
    	
    	return foundTagOption;
    }

}
