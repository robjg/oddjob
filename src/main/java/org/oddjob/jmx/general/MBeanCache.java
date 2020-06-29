package org.oddjob.jmx.general;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;

/**
 * Something capable of creating and caching {@link MBeanNode}s.
 * 
 * @author rob
 *
 */
public interface MBeanCache {

	/**
	 * Find a single MBean matching the object name. If the object
	 * name is a wild card there must be only one match.
	 * 
	 * @param objectName
	 * 
	 * @return A single node. Never null.
	 * 
	 * @throws IntrospectionException
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	MBeanNode findBean(ObjectName objectName)
	throws IntrospectionException, InstanceNotFoundException, ReflectionException, IOException;
	
	/**
	 * Find all MBeans matching the object name. The object name is 
	 * expected to be a wild card.
	 * 
	 * @param objectName
	 * @return An array of 0 or more nodes.
	 * 
	 * @throws IntrospectionException
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	MBeanNode[] findBeans(ObjectName objectName)
	throws IntrospectionException, InstanceNotFoundException, ReflectionException, IOException;
}
