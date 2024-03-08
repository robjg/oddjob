/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.tools;

import org.oddjob.*;
import org.oddjob.arooa.*;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaHandler;
import org.oddjob.arooa.parsing.PrefixMappings;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.runtime.ConfigurationNode;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeListener;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;
import org.oddjob.io.BufferType;
import org.oddjob.io.CopyJob;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Useful utility methods and constants for tests.
 * 
 * @author Rob Gordon.
 */
public class OddjobTestHelper {

	public static final long TEST_TIMEOUT = 10000L;
	
	public static final String LS = System.lineSeparator();
	
	/**
	 * Get the state of a component. Assumes the component is 
	 * {@link Stateful} and throw an Exception if it isn't.
	 * 
	 * @param o
	 * @return
	 */
	public static State getJobState(Object o) {
	    class StateCatcher implements StateListener {
		    State state;
	        public void jobStateChange(StateEvent event) {
	            state = event.getState();
	        }
	    }

        Stateful stateful = (Stateful) o;
	    StateCatcher listener = new StateCatcher();
	    stateful.addStateListener(listener);
	    stateful.removeStateListener(listener);
	    return listener.state;
	}
    
	public static Object[] getChildren(Object o) {
		class ChildCatcher implements StructuralListener {
			final List<Object> results = new ArrayList<>();
			public void childAdded(StructuralEvent event) {
				synchronized (results) {
					results.add(event.getIndex(), event.getChild());
				}
			}
			public void childRemoved(StructuralEvent event) {
				synchronized (results) {
					results.remove(event.getIndex());
				}
			}
		}
		Structural structural = (Structural) o;
		ChildCatcher cc = new ChildCatcher();
		structural.addStructuralListener(cc);
		structural.removeStructuralListener(cc);		
		return cc.results.toArray();
	}
    
	public static String getIconId(Object object) {
		class IconCatcher implements IconListener {
			String iconId;
			
			public void iconEvent(IconEvent e) {
				iconId = e.getIconId();
			}
		}
		
		Iconic iconic = (Iconic) object;
		
		IconCatcher listener = new IconCatcher();
		iconic.addIconListener(listener);
		iconic.removeIconListener(listener);
		return listener.iconId;
	}

	/**
	 * Copy and Object using Serialisation.
	 * 
	 * @param object The object.
	 * 
	 * @return A Copy.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T copy(T object) throws IOException, ClassNotFoundException {
		
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutput oo = new ObjectOutputStream(out);
		oo.writeObject(object);
		oo.close();
			
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		ObjectInput oi = new ObjectInputStream(in);
		Object o = oi.readObject();
		oi.close();
		
		return (T) o;
    }

    public static class Surrogate {
    	Object value;
    	public void addConfiguredWhatever(Object value) {
    		this.value = value;
    	}
    }
    
    public static Object createValueFromXml(String xml) 
    throws ArooaParseException {
	    
    	return createValueFromConfiguration(new XMLConfiguration("TEST", xml));
    }

    public static Object createValueFromConfiguration(ArooaConfiguration config) 
    throws ArooaParseException {
	    
    	ArooaDescriptor descriptor = new OddjobDescriptorFactory(
			).createDescriptor(null);
    	
    	return createValueFromConfiguration(
    			config, descriptor);
    }
    
    public static Object createValueFromConfiguration(ArooaConfiguration config,
    		ArooaDescriptor descriptor) 
    throws ArooaParseException {
	    
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.setArooaType(ArooaType.VALUE);
    	
    	parser.parse(config);

    	return parser.getRoot();        
    }
    
    public static Object createComponentFromXml(String xml) 
    throws ArooaParseException {
	    
    	return createComponentFromConfiguration(new XMLConfiguration("TEST", xml));
    }

	public static Object createComponentFromConfiguration(ArooaConfiguration config)
			throws ArooaParseException {
		return createComponentFromConfiguration(config, false);
	}

    public static Object createComponentFromConfiguration(ArooaConfiguration config,
														  boolean configure)
    throws ArooaParseException {
	    
    	ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	StandardFragmentParser parser = new StandardFragmentParser(session);
    	
    	parser.setArooaType(ArooaType.COMPONENT);
    	
    	ConfigurationHandle<ArooaContext> handler = parser.parse(config);

		if (configure) {
			ArooaContext context = handler.getDocumentContext();
			context.getRuntime().configure();
		}

    	return parser.getRoot();        
    }
    
    /**
     * Register a component with a session. Components must be registered
     * with a context. This method creates a dummy context which does not
     * support all the functionality of a context.
     * 
     * @param component
     * @param session
     * @param id
     */
    public static void register(Object component, final ArooaSession session, String id) {
    	
		class OurContext implements ArooaContext {
			@Override
			public ArooaSession getSession() {
				return session;
			}
			@Override
			public RuntimeConfiguration getRuntime() {
				return new RuntimeConfiguration() {
					@Override
					public void init() throws ArooaConfigurationException {
						throw new UnsupportedOperationException();
					}
					@Override
					public void configure() {
					}
					@Override
					public void destroy() throws ArooaConfigurationException {
						throw new UnsupportedOperationException();
					}
					@Override
					public ArooaClass getClassIdentifier() {
						throw new UnsupportedOperationException();
					}
					@Override
					public void setProperty(String name, Object value)
							throws ArooaPropertyException {
						throw new UnsupportedOperationException();
					}
					@Override
					public void setIndexedProperty(String name, int index,
							Object value) throws ArooaPropertyException {
						throw new UnsupportedOperationException();
					}
					@Override
					public void setMappedProperty(String name, String key,
							Object value) throws ArooaPropertyException {
						throw new UnsupportedOperationException();
					}
					@Override
					public void addRuntimeListener(RuntimeListener listener) {
						throw new UnsupportedOperationException();
					}
					@Override
					public void removeRuntimeListener(RuntimeListener listener) {
						throw new UnsupportedOperationException();
					}
				};
			}
			@Override
			public ArooaHandler getArooaHandler() {
				throw new UnsupportedOperationException();
			}
			@Override
			public ArooaType getArooaType() {
				throw new UnsupportedOperationException();
			}
			@Override
			public ConfigurationNode<ArooaContext> getConfigurationNode() {
				throw new UnsupportedOperationException();
			}
			@Override
			public ArooaContext getParent() {
				throw new UnsupportedOperationException();
			}
			@Override
			public PrefixMappings getPrefixMappings() {
				throw new UnsupportedOperationException();
			}
		}

		session.getComponentPool().registerComponent(
				new ComponentTrinity(
			component, component, new OurContext()), id);
    }
    
    /**
     * Get a work directory creating a new one if necessary.
     * 
     * @return
     */
    public static File getWorkDir() {
    	
    	File file = new File("work");
    	
    	if (!file.exists()) {
    		file.mkdir();
    	}
    	
    	return file;
    }
    
    public static String[] streamToLines(InputStream inputStream) {
    	
		BufferType buffer = new BufferType();
		buffer.configured();
		
		CopyJob copy = new CopyJob();
		copy.setInput(inputStream);
		copy.setOutput(buffer.toOutputStream());
		copy.run();
		
		return buffer.getLines();
    }
}
