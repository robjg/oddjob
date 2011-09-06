package org.oddjob;

import java.util.Properties;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.EmptyDescriptor;
import org.oddjob.arooa.deploy.LinkedDescriptor;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.registry.SimpleComponentPool;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.ExtendedTools;
import org.oddjob.arooa.standard.StandardArooaDescriptor;
import org.oddjob.arooa.standard.StandardPropertyManager;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.persist.OddjobPersister;

/**
 * Constructs an {@link ArooaSession} for Oddjob. This is quite complicated
 * because of the ability to nest one Oddjob inside another.
 * 
 * @author rob
 *
 */
public class OddjobSessionFactory {

	private ArooaSession existingSession;
	
	private ClassLoader classLoader;
	
	private ArooaDescriptorFactory descriptorFactory;
	
	private OddjobPersister oddjobPersister;
	
	/** Explicit properties to set in the new PropertyManager. */
	private Properties properties;
	
	private OddjobInheritance inherit;
	
	/**
	 * Create a session without any persister. Used for testing.
	 * 
	 * @return A session. Never null.
	 */
	public ArooaSession createSession() {
		return createSession(null);
	}
	
	/**
	 * Create a session.
	 * 
	 * @param oddjob Used to find the id of Oddjob in any existing session
	 * if a persister is being used.
	 * 
	 * @return A session. Never null.
	 */
	public ArooaSession createSession(Object oddjob) {

		ComponentProxyResolver componentProxyResolver = null;
		
		ClassLoader classLoader = this.classLoader;
		if (classLoader == null) {
			classLoader = getClass().getClassLoader();
		}
		
		ArooaDescriptor descriptor = null;
		
		if (descriptorFactory != null) {
			descriptor = descriptorFactory.createDescriptor(
					classLoader);
		}
		
		ComponentPersister componentPersister = null;
		
		String oddjobId = null;
		if (existingSession != null) {
			oddjobId = existingSession.getBeanRegistry().getIdFor(oddjob);
		}
				
		if (oddjobPersister != null) {
			componentPersister = oddjobPersister.persisterFor(oddjobId);
		}
		
		ArooaTools tools = null;
		PropertyManager propertyManager = null;
		BeanRegistry beanRegistry = null;

    	if (existingSession == null) {
        	
    		ArooaDescriptor mainDescriptor = 
    			new OddjobDescriptorFactory(
    					).createDescriptor(classLoader);
        	
    		ArooaDescriptor oddjobDescriptor = new LinkedDescriptor(
    				mainDescriptor,
    				new StandardArooaDescriptor());
    		
            if (descriptor == null) {
                	
        		descriptor = oddjobDescriptor;
        	} 
        	else {
        		descriptor = new LinkedDescriptor(descriptor, 
    					oddjobDescriptor);
        	}
            
            tools = new ExtendedTools(new StandardTools(), descriptor);
    	}
    	else {
    		
    		tools = existingSession.getTools();
    		
    		if (this.classLoader != null && descriptor == null) {
    			descriptor = new EmptyDescriptor(classLoader);
    		}
    		
    		if (descriptor == null) {
    			descriptor = existingSession.getArooaDescriptor();
    			if (descriptor == null) {
    				throw new NullPointerException(
    						"Existing session has no ArooaDescriptor.");
    			}
    		}
    		else {
    			descriptor = new LinkedDescriptor(descriptor, 
    					existingSession.getArooaDescriptor());
    			
    			tools = new ExtendedTools(tools, descriptor);
    		}
    		
    		componentProxyResolver = 
    			existingSession.getComponentProxyResolver();

    		if (componentPersister == null && oddjobId != null) {
	    		componentPersister = existingSession.getComponentPersister();
	    		
	    		if (componentPersister instanceof OddjobPersister) {
	    			componentPersister = ((OddjobPersister) componentPersister
	    					).persisterFor(oddjobId);
	    		}
    		}

    		if (inherit == null) {
    			inherit = OddjobInheritance.PROPERTIES;
    		}
    		
    		switch (inherit) {
    		case NONE:
                propertyManager = new StandardPropertyManager(properties);
    			break;
    		case PROPERTIES:
    			propertyManager = new StandardPropertyManager(
        				existingSession.getPropertyManager(), properties);
    			break;
    		case SHARED:
    			propertyManager = existingSession.getPropertyManager();
    			beanRegistry = existingSession.getBeanRegistry();
    			break;
    		}
    	}
    			
		if (componentProxyResolver == null) {
			componentProxyResolver = new OddjobComponentResolver();
		}
		if (propertyManager == null) {
			propertyManager = new StandardPropertyManager(properties);
		}
		if (beanRegistry == null) {
			beanRegistry = new SimpleBeanRegistry(
	    			tools.getPropertyAccessor(),
	    			tools.getArooaConverter());
		}
		
		final ArooaDescriptor finalDescriptor = descriptor;
    	final ComponentPool finalComponentPool = new SimpleComponentPool();
    	final ArooaTools finalTools = tools;
    	final BeanRegistry finalBeanRegistry = beanRegistry;
    	final ComponentPersister finalComponentPersister = componentPersister;
    	final ComponentProxyResolver finalComponentProxyResolver =
    		componentProxyResolver;
    	final PropertyManager finalPropertyManager = propertyManager;
    	
    	
		return new ArooaSession() {
			@Override
			public ArooaDescriptor getArooaDescriptor() {
				return finalDescriptor;
			}
			@Override
			public ComponentPool getComponentPool() {
				return finalComponentPool;
			}
			@Override
			public BeanRegistry getBeanRegistry() {
				return finalBeanRegistry;
			}
			@Override
			public PropertyManager getPropertyManager() {
				return finalPropertyManager;
			}
			@Override
			public ArooaTools getTools() {
				return finalTools;
			}
			@Override
			public ComponentPersister getComponentPersister() {
				return finalComponentPersister;
			}
			@Override
			public ComponentProxyResolver getComponentProxyResolver() {
				return finalComponentProxyResolver;
			}
		};
	}

	public ArooaSession getExistingSession() {
		return existingSession;
	}

	public void setExistingSession(ArooaSession existingSession) {
		this.existingSession = existingSession;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ArooaDescriptorFactory getDescriptorFactory() {
		return descriptorFactory;
	}

	public void setDescriptorFactory(ArooaDescriptorFactory descriptorFactory) {
		this.descriptorFactory = descriptorFactory;
	}

	public OddjobPersister getOddjobPersister() {
		return oddjobPersister;
	}

	public void setOddjobPersister(OddjobPersister oddjobPersister) {
		this.oddjobPersister = oddjobPersister;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public OddjobInheritance isInherit() {
		return inherit;
	}

	public void setInherit(OddjobInheritance inherit) {
		this.inherit = inherit;
	}
	
}
