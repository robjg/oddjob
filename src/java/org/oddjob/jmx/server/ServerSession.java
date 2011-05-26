package org.oddjob.jmx.server;

import javax.management.JMException;
import javax.management.ObjectName;

import org.oddjob.jmx.ObjectNames;

public interface ServerSession extends ObjectNames {

	public ObjectName createMBeanFor(Object child, 
			ServerContext childContext)
	throws JMException;

	public void destroy(ObjectName childName)
	throws JMException;

}
