package org.oddjob.jmx.client;

import java.io.ObjectInputStream;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

public class MockMBeanServer implements MBeanServer {

	public void addNotificationListener(ObjectName name,
			NotificationListener listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void addNotificationListener(ObjectName name, ObjectName listener,
			NotificationFilter filter, Object handback)
			throws InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInstance createMBean(String className, ObjectName name)
			throws ReflectionException, InstanceAlreadyExistsException,
			MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInstance createMBean(String className, ObjectName name,
			ObjectName loaderName) throws ReflectionException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			MBeanException, NotCompliantMBeanException,
			InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInstance createMBean(String className, ObjectName name,
			Object[] params, String[] signature) throws ReflectionException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			MBeanException, NotCompliantMBeanException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInstance createMBean(String className, ObjectName name,
			ObjectName loaderName, Object[] params, String[] signature)
			throws ReflectionException, InstanceAlreadyExistsException,
			MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInputStream deserialize(ObjectName name, byte[] data)
			throws InstanceNotFoundException, OperationsException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInputStream deserialize(String className, byte[] data)
			throws OperationsException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInputStream deserialize(String className,
			ObjectName loaderName, byte[] data)
			throws InstanceNotFoundException, OperationsException,
			ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Object getAttribute(ObjectName name, String attribute)
			throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public AttributeList getAttributes(ObjectName name, String[] attributes)
			throws InstanceNotFoundException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ClassLoader getClassLoader(ObjectName loaderName)
			throws InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ClassLoader getClassLoaderFor(ObjectName mbeanName)
			throws InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ClassLoaderRepository getClassLoaderRepository() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public String getDefaultDomain() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public String[] getDomains() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Integer getMBeanCount() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public MBeanInfo getMBeanInfo(ObjectName name)
			throws InstanceNotFoundException, IntrospectionException,
			ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInstance getObjectInstance(ObjectName name)
			throws InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Object instantiate(String className) throws ReflectionException,
			MBeanException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Object instantiate(String className, ObjectName loaderName)
			throws ReflectionException, MBeanException,
			InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Object instantiate(String className, Object[] params,
			String[] signature) throws ReflectionException, MBeanException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Object instantiate(String className, ObjectName loaderName,
			Object[] params, String[] signature) throws ReflectionException,
			MBeanException, InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Object invoke(ObjectName name, String operationName,
			Object[] params, String[] signature)
			throws InstanceNotFoundException, MBeanException,
			ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public boolean isInstanceOf(ObjectName name, String className)
			throws InstanceNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public boolean isRegistered(ObjectName name) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ObjectInstance registerMBean(Object object, ObjectName name)
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void removeNotificationListener(ObjectName name, ObjectName listener)
			throws InstanceNotFoundException, ListenerNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void removeNotificationListener(ObjectName name,
			NotificationListener listener) throws InstanceNotFoundException,
			ListenerNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void removeNotificationListener(ObjectName name,
			ObjectName listener, NotificationFilter filter, Object handback)
			throws InstanceNotFoundException, ListenerNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void removeNotificationListener(ObjectName name,
			NotificationListener listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException,
			ListenerNotFoundException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void setAttribute(ObjectName name, Attribute attribute)
			throws InstanceNotFoundException, AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public AttributeList setAttributes(ObjectName name, AttributeList attributes)
			throws InstanceNotFoundException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void unregisterMBean(ObjectName name)
			throws InstanceNotFoundException, MBeanRegistrationException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
