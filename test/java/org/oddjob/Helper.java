/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * Useful utilities for tests.
 * 
 * @author Rob Gordon.
 */
public class Helper {

	public static final long TEST_TIMEOUT = 10000L;
	
	public static final String LS = System.getProperty("line.separator");
	
	public static JobState getJobState(Object o) {
	    class StateCatcher implements JobStateListener {
		    JobState jobState;
	        public void jobStateChange(JobStateEvent event) {
	            jobState = event.getJobState();
	        }
	    };
	    
		Stateful stateful = (Stateful) o;
	    StateCatcher listener = new StateCatcher();
	    stateful.addJobStateListener(listener);
	    stateful.removeJobStateListener(listener);
	    return listener.jobState;
	}
    
	public static Object[] getChildren(Object o) {
		class ChildCatcher implements StructuralListener {
			List<Object> results = new ArrayList<Object>();
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
    
    public static Object createTypeFromXml(String xml) 
    throws ArooaParseException {
	    
    	return createTypeFromConfiguration(new XMLConfiguration("TEST", xml));
    }

    public static Object createTypeFromConfiguration(ArooaConfiguration config) 
    throws ArooaParseException {
	    
    	ArooaDescriptor descriptor = new OddjobDescriptorFactory(
			).createDescriptor(null);
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.setArooaType(ArooaType.VALUE);
    	
    	parser.parse(config);

    	return parser.getRoot();        
    }
    
    public static Object createComponentFromXml(String xml) 
    throws IOException, ArooaParseException {
	    
    	return createComponentFromConfiguration(new XMLConfiguration("TEST", xml));
    }
    
    public static Object createComponentFromConfiguration(ArooaConfiguration config) 
    throws ArooaParseException {
	    
    	ArooaDescriptor descriptor = new OddjobDescriptorFactory(
			).createDescriptor(null);
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.setArooaType(ArooaType.COMPONENT);
    	
    	parser.parse(config);

    	return parser.getRoot();        
    }
    
    public static void register(Object component, final ArooaSession session, String id) {
    	
		class OurContext extends MockArooaContext {
			@Override
			public ArooaSession getSession() {
				return session;
			}
			@Override
			public RuntimeConfiguration getRuntime() {
				return new MockRuntimeConfiguration() {
					@Override
					public void configure() {
					}
				};
			}
		}

		session.getComponentPool().registerComponent(
				new ComponentTrinity(
			component, component, new OurContext()), id);
    }
    
    public static File getWorkDir() {
    	
    	File file = new File("work");
    	
    	if (!file.exists()) {
    		file.mkdir();
    	}
    	
    	return file;
    }
}
