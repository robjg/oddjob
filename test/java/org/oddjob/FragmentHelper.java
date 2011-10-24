package org.oddjob;

import java.util.Properties;

import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.xml.XMLConfiguration;

public class FragmentHelper {

	private Properties properties;
	
	private ArooaSession session;
	
	public ArooaSession getSession() {
		return session;
	}

	public Object createComponentFromResource(String resource) 
    throws ArooaParseException {
	    
		return createFromResource(ArooaType.COMPONENT, resource);
    }
    
	public Object createValueFromResource(String resource) 
    throws ArooaParseException {
	    
		return createFromResource(ArooaType.VALUE, resource);
    }
	
	private Object createFromResource(ArooaType type, String resource) 
	throws ArooaParseException {
		
    	OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
    	sessionFactory.setProperties(properties);

    	session = sessionFactory.createSession();
    	
    	StandardFragmentParser parser = new StandardFragmentParser(session);
    	
    	parser.setArooaType(type);
    	
    	ArooaConfiguration config = new XMLConfiguration(resource,
    			getClass().getClassLoader());
    	
    	parser.parse(config);

    	return parser.getRoot();        
	}
	
    public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
