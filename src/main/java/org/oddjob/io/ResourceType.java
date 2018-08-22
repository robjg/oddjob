/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.ClassResolver;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;

/**
 * @oddjob.description Specify a resource on the class path. 
 * <p>
 * This uses
 * Oddjob's internal class path to find the resource which includes all
 * Oddballs. Oddballs will be searched in the order they were loaded.
 * 
 * @oddjob.example
 * 
 * Specifiy properties as a resource on the class path.
 * 
 * <pre>
 * &lt;variables id='props'&gt;
 *   &lt;properties&gt;
 *      &lt;resource resource="org/oddjob/AResource.props"/&gt;
 *   &lt;/properties&gt;
 * &lt;/variables&gt;
 * </pre>
 * 
 * @author Rob Gordon.
 */
public class ResourceType implements ArooaValue, ArooaSessionAware {

	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
	    	
	    	registry.register(ResourceType.class, InputStream.class, 
	    			new Convertlet<ResourceType, InputStream>() {
	    		public InputStream convert(ResourceType from) throws ConvertletException {
	    			try {
		    			return from.toInputStream();
	    			}
	    			catch (IOException e) {
	    				throw new ConvertletException(e);
	    			}
	    		}
	    	});
	    	
	    	registry.register(ResourceType.class, URL.class, 
	    			new Convertlet<ResourceType, URL>() {
	    		public URL convert(ResourceType from) throws ConvertletException {
		    		return from.toURL();
	    		}
	    	});
	    	
	    	registry.register(ResourceType.class, String.class, 
	    			new Convertlet<ResourceType, String>() {
	    		public String convert(ResourceType from) throws ConvertletException {
	        		return from.resource;
	    		}
	    	});
		}		
	}
	
    /**
     * @oddjob.property
     * @oddjob.description The resource
     * @oddjob.required Yes.
     */
    private String resource;
        
    private ArooaSession session;
    
    public ResourceType() {
	}
    
    public ResourceType(String resource) {
    	this.resource = resource;
	}
    
    @Override
    @ArooaHidden
    public void setArooaSession(ArooaSession session) {
    	this.session = session;
    }
    
    public URL toURL() {
		URL url = null;
		if (session == null) {
			url = getClass().getClassLoader().getResource(
					resource);
		}
		else {
			ClassResolver resolver = 
				session.getArooaDescriptor(
					).getClassResolver();
			url = resolver.getResource(resource);
		}
		return url;
    }
    
    public InputStream toInputStream() throws IOException {
		
    	URL url = toURL();
    	
		if (url == null) {
			throw new IOException("No Resource found: " + resource);
		}
		
		return url.openStream();
    }
    
    
    /**
     * Set the resource.
     * 
     * @param resource The resource.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }
        
    public String toString() {
    	return resource;
    }
}
