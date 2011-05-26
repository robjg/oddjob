package org.oddjob.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.oddjob.arooa.beanutils.MagicBeanDefinition;
import org.oddjob.arooa.beanutils.MagicBeanProperty;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;

public class ResultSetBeanFactory {

	private static AtomicInteger instance = new AtomicInteger();
	
	private final ResultSet resultSet;
	
	private final ArooaClass arooaClass;

	private final PropertyAccessor accessor;
	
	public ResultSetBeanFactory(ResultSet resultSet,
			PropertyAccessor accessor,
			ClassLoader loader) throws SQLException, ClassNotFoundException {
		
		this.accessor = accessor;
		
		MagicBeanDefinition magicDef = new MagicBeanDefinition();
		magicDef.setName("QueryBean-" + instance.getAndIncrement());
	
		ResultSetMetaData metaData = resultSet.getMetaData();
		
		for (int i = 1; i <= metaData.getColumnCount(); ++i) {
			
			MagicBeanProperty prop = new MagicBeanProperty();
			prop.setName(metaData.getColumnName(i));
			prop.setType(metaData.getColumnClassName(i));
			
			magicDef.setProperties(i - 1, prop);
		}
		
		this.resultSet = resultSet;
		this.arooaClass = magicDef.createMagic(loader);
	}
	
	public Object next() throws SQLException {
		if (!resultSet.next()) {
			return null;
		}
		
		Object bean = arooaClass.newInstance();
		
		BeanOverview overview = arooaClass.getBeanOverview(accessor);
		
		String[] properties = overview.getProperties();
		for (int i = 0; i < properties.length; ++i) {
			accessor.setProperty(bean, properties[i], 
					resultSet.getObject(i + 1));
		}
		return bean;
	}
	
	public List<Object> all() throws SQLException {
	
		List<Object> all = new ArrayList<Object>();
		for (Object next = next(); next != null; next = next()) {
			all.add(next);
		}
		return all;
	}
}
