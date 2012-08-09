package org.oddjob.jmx.general;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.oddjob.arooa.ClassResolver;

/**
 * A Simple Map based implementation of a {@link MBeanCache}.
 * 
 * @author rob
 *
 */
public class MBeanCacheMap implements MBeanCache {

	private final MBeanServerConnection mBeanServer;
	
	private final ClassResolver classRresolver;
	
	private final Map<ObjectName, MBeanNode> beans = 
			new HashMap<ObjectName, MBeanNode>();

	public MBeanCacheMap(MBeanServerConnection mBeanServer,
			ClassResolver classResolver) {
		this.mBeanServer = mBeanServer;
		this.classRresolver = classResolver;
	}
	
	public MBeanNode findBean(ObjectName objectName) 
	throws IntrospectionException, InstanceNotFoundException, ReflectionException, IOException {
		
		MBeanNode bean = beans.get(objectName);

		if (bean == null) {
			
			MBeanNode[] beans = findBeans(objectName);
			
			if (beans.length == 0)  {
				throw new IllegalArgumentException(
						"No MBean found for " + 
								objectName);
			}
			
			if (beans.length > 1)  {
				throw new IllegalArgumentException(
						"More than one MBean Found for " + 
								objectName);
			}
			
			bean = beans[0];
		}
		
		return bean;
		
	}
	
	public MBeanNode[] findBeans(ObjectName objectName) 
	throws IntrospectionException, InstanceNotFoundException, ReflectionException, IOException {
		
		Set<ObjectName> names = mBeanServer.queryNames(
				objectName, null);
		
		MBeanNode[] wrappers = new MBeanNode[names.size()];
		
		int i = 0;
		for (ObjectName name: names) {
			
			MBeanNode wrapper = beans.get(name);
			
			if (wrapper == null) {
				
				wrapper = new SimpleMBeanNode(name, 
						mBeanServer, classRresolver);
				beans.put(name, wrapper);
			}
			
			wrappers[i++] = wrapper;
		}
		
		return wrappers;
	}
}
