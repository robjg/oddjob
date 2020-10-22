package org.oddjob.jmx.general;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.oddjob.Describable;
import org.oddjob.Iconic;
import org.oddjob.arooa.ClassResolver;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageData;
import org.oddjob.logging.LogEnabled;
import org.oddjob.script.InvokerArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.io.IOError;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Simple implementation of an {@link MBeanNode}.
 * 
 * @author rob
 *
 */
public class SimpleMBeanNode implements 
		MBeanNode, Describable, LogEnabled, Iconic {

	/** For logger names. */
	private static final AtomicInteger instanceCount = new AtomicInteger();
	
	/** The icon. */
	private static final ImageData icon;

	static {
		try {
			icon = ImageData.fromUrl(
					SimpleDomainNode.class.getResource("mbean.gif"),
					"MBean");
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	/** The logger for this instance. */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName() + 
			"." + instanceCount.incrementAndGet());
	
	/** Then name. */
	private final ObjectName objectName;

	/** The server. */
	private final MBeanServerConnection mBeanServer;
	
	/** For classes. */
	private final ClassResolver classResolver;
	
	/** Info. */
	private final MBeanInfo info;
	
	/** For DynaBean. */
	private final ThisDynaClass dynaClass;
	
	/**
	 * Constructor
	 * 
	 * @param objectName
	 * @param mBeanServer
	 * @param classResolver
	 * 
	 * @throws IntrospectionException
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 */
	public SimpleMBeanNode(ObjectName objectName, 
			MBeanServerConnection mBeanServer,
			ClassResolver classResolver) 
	throws IntrospectionException, InstanceNotFoundException, ReflectionException, IOException {
		
		this.objectName = objectName;
		this.mBeanServer = mBeanServer;
		this.classResolver = classResolver;
		
		this.info = mBeanServer.getMBeanInfo(objectName);
		
		dynaClass = new ThisDynaClass(info.getAttributes());	
	}
	
	@Override
	public void initialise() {
		logger.info("MBean: " + objectName);
		logger.info("Attributes:");
		MBeanAttributeInfo[] attributeInfo = info.getAttributes();
		for (MBeanAttributeInfo attr : attributeInfo) {
			logger.info("  " + attr.getName() + ": " + attr.getType());
		}
		logger.info("Operations:");
		MBeanOperationInfo[] operationInfo = info.getOperations();
		for (MBeanOperationInfo op : operationInfo) {
			StringBuilder params = new StringBuilder();
			for (MBeanParameterInfo param : op.getSignature()) {
				if (params.length() > 0) {
					params.append(", ");
				}
				params.append(param.getType());
			}
			logger.info("  " + op.getName() + "(" + params.toString() + 
					"): " + op.getReturnType());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.logging.LogEnabled#loggerName()
	 */
	@Override
	public String loggerName() {
		return logger.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.oddjob.script.Invoker#invoke(java.lang.String, org.oddjob.script.InvokerArguments)
	 */
	@Override
	public Object invoke(String name, InvokerArguments args) 
	throws Exception {
		
		MBeanOperationInfo[] opInfos = info.getOperations();
		
		MBeanOperationInfo match = null;
		for (MBeanOperationInfo info : opInfos) {
			if (name.equals(info.getName()) && 
					args.size()== info.getSignature().length) {
				
				if (match != null) {
					throw new IllegalArgumentException(
							"Failed to find a unique method " + name + 
							" with " + args.size() + 
							" args. (We don't do overriding yet!)");
				}
				
				match = info;
			}
		}

		if (match == null) {
			throw new IllegalArgumentException(
				"Failed to find a method " + name + 
				" with " + args.size() + " args.");
		}
		
		MBeanParameterInfo[] paramInfo = match.getSignature();
		String[] signature = new String[paramInfo.length];
		
		Object[] converted  = new Object[signature.length];
		for (int i = 0; i < signature.length; ++i) {
			signature[i] = paramInfo[i].getType();
			Class<?> type = classResolver.findClass(signature[i]);
			if (type == null) {
				throw new RuntimeException("Can not resolve class " +
						signature[i] + " for argument " + i);
			}
			try {
				converted[i] = args.getArgument(i, type);
			} catch (ArooaConversionException e) {
				throw new IllegalArgumentException(
						"Failed to convert argument" + i, e);
			}
		}
		
		try {
			logger.info("Invoking " + name + " with args " +  
					Arrays.toString(converted));
			
			Object result = mBeanServer.invoke(
					objectName, name, converted, signature);
			
			logger.info("Successfully invoked " + name + ", result " +
					result);
			
			return result;
			
		} catch (Exception e) {
			logger.warn("Failed invoking" + name + "." +  
					Arrays.toString(converted));
			
			throw e;
		}
	}
	
	private static final String MBEAN_ICON = "mbean";
	
	@Override
	public void addIconListener(IconListener listener) {
		listener.iconEvent(new IconEvent(this, MBEAN_ICON));
	}
	
	@Override
	public ImageData iconForId(String id) {
		return icon;
	}
	
	@Override
	public void removeIconListener(IconListener listener) {
	}
	
	@Override
	public String toString() {
		return objectName.toString();
	}

	@Override
	public boolean contains(String arg0, String arg1) {
		return false;
	}

	@Override
	public Object get(String name) {
		try {
			Object result = mBeanServer.getAttribute(objectName, name);
			if (result instanceof CompositeData) {
				return new CompositeDataDynaBean(
						(CompositeData) result);
			}
			else {
				return result;
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to get attribute " + name,
					e);
		}
	}

	@Override
	public Object get(String arg0, int arg1) {
		throw new RuntimeException("No indexed properties.");
	}

	@Override
	public Object get(String arg0, String arg1) {
		throw new RuntimeException("No mapped properties.");
	}

	@Override
	public DynaClass getDynaClass() {
		return dynaClass;
	}

	@Override
	public void remove(String arg0, String arg1) {
		throw new RuntimeException("No mapped properties.");
	}

	@Override
	public void set(String name, Object value) {
		try {
			logger.info("Setting " + name + " to " + value);
			
			mBeanServer.setAttribute(objectName, 
					new Attribute(name, value));
		} catch (Exception e) {
			throw new RuntimeException("Failed to get attribute " + name,
					e);
		}
	}

	@Override
	public void set(String arg0, int arg1, Object arg2) {
		throw new RuntimeException("No indexed properties.");
	}

	@Override
	public void set(String arg0, String arg1, Object arg2) {
		throw new RuntimeException("No mapped properties.");
	}
	
	@Override
	public Map<String, String> describe() {
		
		Map<String, String> description =
				new LinkedHashMap<>();
		
		DynaProperty[] props = dynaClass.getDynaProperties();

		for (DynaProperty prop : props) {
			
			Object value = get(prop.getName());
			
			description.put(prop.getName(), 
					value == null ? null : value.toString());
		}
		
		return description;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.client.Destroyable#destroy()
	 */
	@Override
	public void destroy() {
	}
	
	/**
	 * The {@link DynaClass} implementation.
	 */
	private class ThisDynaClass implements Serializable, DynaClass {
		private static final long serialVersionUID = 2012080200L;
		
		private final AttributeDynaProperty[] properties;
		
		private final Map<String, AttributeDynaProperty> map =
				new HashMap<>();
		
		public ThisDynaClass(MBeanAttributeInfo[] attributes) {

			this.properties = new AttributeDynaProperty[attributes.length];
			
			for (int i = 0; i < properties.length; ++i) {
				
				MBeanAttributeInfo info = attributes[i];
				
				AttributeDynaProperty property = 
						new AttributeDynaProperty(info);
				
				properties[i] = property;
				map.put(property.getName(), property);
			}
		}
		
		@Override
		public DynaProperty[] getDynaProperties() {
			return properties;
		}
		
		@Override
		public DynaProperty getDynaProperty(String name) {
			return map.get(name);
		}
		
		@Override
		public String getName() {
			return SimpleMBeanNode.this.toString() ;
		}
		
		@Override
		public DynaBean newInstance() throws
				InstantiationException {
			throw new InstantiationException(
					"Can't create new " + getName());
		}
	}
	
	/**
	 * The {@link DynaProperty} implementation.
	 *
	 */
	private class AttributeDynaProperty extends DynaProperty {
		private static final long serialVersionUID = 2012080200L;
		
		private final MBeanAttributeInfo info;
		
		public AttributeDynaProperty(MBeanAttributeInfo info) {
			super(info.getName());
			this.info = info;
		}
		
		@Override
		public Class<?> getType() {
			return classResolver.findClass(info.getType());
		}		
	}
}
