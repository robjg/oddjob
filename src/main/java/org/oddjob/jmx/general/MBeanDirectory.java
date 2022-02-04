package org.oddjob.jmx.general;

import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.registry.BeanDirectory;

import javax.management.ObjectName;
import java.text.ParseException;

/**
 * An implementation of an {@link BeanDirectory} for accessing
 * {@link MBeanNode}s.
 * <p>
 * {@link #getIdFor(Object)} and {@link #getAllByType(Class)} are not
 * currently supported.
 * 
 * @author rob
 *
 */
public class MBeanDirectory implements BeanDirectory {

	private final PropertyAccessor accessor;
	
	private final ArooaConverter converter;
	
	private final MBeanCache cache;
	
	public MBeanDirectory(
			MBeanSession session) {
		
		ArooaTools tools = session.getArooaSession().getTools();
		this.converter = tools.getArooaConverter();
		this.accessor = tools.getPropertyAccessor(
				).accessorWithConversions(converter);		
				
		this.cache = session.getMBeanCache();
	}

	private Object mBeanLookup(MBeanDirectoryPathParser parser,
			String path) throws ArooaPropertyException {
		
		try {
			parser.parse(path);
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					"Failed to parse MBean property path" + path, e);
		}
		
		if (parser.getName() == null) {
			return null;
		}
		
		try {
			ObjectName objectName = new ObjectName(parser.getName());
			
			return  cache.findBean(objectName);
		} catch (Exception e) {
			throw new ArooaPropertyException(
					path, "Failed to find bean.", e);
		}
	}	
	
	@Override
	public Object lookup(String path) throws ArooaPropertyException {
		
		MBeanDirectoryPathParser parser = new MBeanDirectoryPathParser();

		Object bean = mBeanLookup(parser, path);
		if (parser.getProperty() == null) {
			return bean;
		}
		
		return  accessor.getProperty(bean, parser.getProperty()); 
	}
	
	@Override
	public <T> T lookup(String path, Class<T> required)
			throws ArooaPropertyException, ArooaConversionException {
		
		MBeanDirectoryPathParser parser = new MBeanDirectoryPathParser();

		Object bean = mBeanLookup(parser, path);
		if (parser.getProperty() == null) {
			return converter.convert(bean, required);
		}
		
		return  accessor.getProperty(bean, parser.getProperty(), required); 
	}

	@Override
	public String getIdFor(Object bean) {
		return null;
	}

	@Override
	public <T> Iterable<T> getAllByType(Class<T> type) {
		throw new UnsupportedOperationException("Can't find all MBeans by type.");
	}
}
