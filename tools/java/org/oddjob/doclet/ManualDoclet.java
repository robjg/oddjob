/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.doclet;

import java.io.File;

import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.beandocs.SessionArooaDocFactory;
import org.oddjob.arooa.beandocs.WriteableArooaDoc;
import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.arooa.deploy.ClassPathDescriptorFactory;
import org.oddjob.arooa.deploy.LinkedDescriptor;
import org.oddjob.arooa.standard.BaseArooaDescriptor;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.util.URLClassLoaderType;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

/**
 * The Doclet for creating the Oddjob reference.
 * 
 * @author Rob Gordon.
 */
public class ManualDoclet {

	private final JobsAndTypes jats;
	private final Archiver archiver;

	public ManualDoclet(String classPath, String descriptorResource) {
		
    	SessionArooaDocFactory docsFactory; 
    		
		ClassPathDescriptorFactory factory
			= new ClassPathDescriptorFactory();
		if (descriptorResource != null) {
			factory.setResource(descriptorResource);
		}

		if (classPath == null) {
			
			ArooaDescriptor descriptor = factory.createDescriptor(
					getClass().getClassLoader());
			
			docsFactory = new SessionArooaDocFactory(
					new StandardArooaSession(descriptor));	
		}
		else {
			File[] files = new FileConvertlets().pathToFiles(classPath);
			URLClassLoaderType classLoaderType = new URLClassLoaderType();
			classLoaderType.setFiles(files);
			classLoaderType.setParent(getClass().getClassLoader());
			classLoaderType.configured();
			
			factory.setExcludeParent(true);
			
			ClassLoader classLoader = classLoaderType.toValue();
			
			ArooaDescriptor thisDescriptor = 
				factory.createDescriptor(classLoader);
			
			if (thisDescriptor == null) {
				throw new NullPointerException("No Descriptor for path " +
						classPath);
			}
			
			ArooaDescriptor descriptor = 
				new LinkedDescriptor(
						thisDescriptor,
						new BaseArooaDescriptor(classLoader));
			
			docsFactory = new SessionArooaDocFactory(
					new StandardArooaSession(), descriptor);
		}
		
    	WriteableArooaDoc jobs = 
    		docsFactory.createBeanDocs(ArooaType.COMPONENT);
    	
    	WriteableArooaDoc types = 
    		docsFactory.createBeanDocs(ArooaType.VALUE);
    	
    	this.jats = new JobsAndTypes(jobs, types);
    	this.archiver = new Archiver(jats);
	}
	
	JobsAndTypes jobsAndTypes() {
		return jats;
	}
	
    void process(ClassDoc cd) {
    	archiver.archive(cd);
    }
    
    void process(RootDoc rootDoc, String destination, String title) {
        ClassDoc[] cd = rootDoc.classes();
        
        System.out.println("ManualDoc: Working through " + 
        		cd.length + " classes.");
        
        for (int i = 0; i < cd.length; ++i) {
            process(cd[i]);
        }
        ManualWriter w = new ManualWriter(destination, title);
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
    	
    	Options options = readOptions(rootDoc.options());
    			
    	ManualDoclet md = new ManualDoclet(
    			options.getDescriptorPath(), options.getResource());
        md.process(rootDoc, options.getDestination(),
        		options.getTitle());
        return true;
    }
    
    private static Options readOptions(String[][] options) {
        Options result = new Options();
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals("-d")) {
            	result.setDestination(opt[1]);
            }
            else if (opt[0].equals("-dp")) {
            	result.setDescriptorPath(opt[1]);            	
            }
            else if (opt[0].equals("-dr")) {
            	result.setResource(opt[1]);       
            }
            else if (opt[0].equals("-t")) {
            	result.setTitle(opt[1]);            	
            }
        }
        return result;
    }

    public static int optionLength(String option) {
    	if (option.equals("-d")) {
    		return 2;
    	}
    	if (option.equals("-dp")) {
    		return 2;
    	}
    	if (option.equals("-dr")) {
    		return 2;
    	}
    	if (option.equals("-t")) {
    		return 2;
    	}
    	return 0;
    }
    public static boolean validOptions(String options[][], 
		       DocErrorReporter reporter) {
    	
    	boolean foundDestination = false;
    	boolean foundPath = false;
    	boolean foundResource = false;
    	boolean foundTitle = false;

    	boolean ok = true;
    	
    	for (int i = 0; i < options.length; i++) {
    		String[] opt = options[i];
    		if (opt[0].equals("-d")) {
    			if (foundDestination) {
    				reporter.printError("Only one -d option allowed.");
    				ok = false;
    			} 
    			else { 
    				foundDestination = true;
    			}
    		}
			if (opt[0].equals("-dp")) {
				if (foundPath) {
					reporter.printError("Only one -dp option allowed.");
					ok = false;
				} 
				else { 
					foundPath= true;
				}
			}
			if (opt[0].equals("-dr")) {
				if (foundResource) {
					reporter.printError("Only one -dr option allowed.");
					ok = false;
				} 
				else { 
					foundResource = true;
				}
			}
			if (opt[0].equals("-t")) {
				if (foundTitle) {
					reporter.printError("Only one -t option allowed.");
					ok = false;
				} 
				else { 
					foundTitle= true;
				}
			}
    	}
    	
    	if (!foundDestination) {
    		ok = false;
    	}
    	
    	if (!ok) {
    		reporter.printError("Usage: javadoc -d destinationDir" +
    				" [-dp arooa-descriptor-path]" +
    				" [-dr arooa-descriptor-resource]" +
    				" [-t titile]" +
    				" -doclet ManualDoclet ...");
    	}
    	
    	return ok;
    }

    private static class Options {
    	
    	private String destination;
    	
    	private String descriptorPath;

    	private String resource;
    	
    	private String title; 
    	
		public String getDestination() {
			return destination;
		}

		public void setDestination(String destination) {
			this.destination = destination;
		}

		public String getDescriptorPath() {
			return descriptorPath;
		}

		public void setDescriptorPath(String resource) {
			this.descriptorPath = resource;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}
    }
}
