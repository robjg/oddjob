package org.oddjob.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.oddjob.arooa.beanutils.MagicBeanClassCreator;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;

/**
 * Helper class that creates beans out of result sets.
 * 
 * @author rob
 *
 */
public class ResultSetBeanFactory {

	private static AtomicInteger instance = new AtomicInteger();
	
	private final ResultSetExtractor resultSetExtractor;
	
	private final ArooaClass arooaClass;

	private final PropertyAccessor accessor;
	
	public ResultSetBeanFactory(ResultSet resultSet,
			PropertyAccessor accessor,
			DatabaseDialect dialect) throws SQLException, ClassNotFoundException {
		
		this.accessor = accessor;
		
		MagicBeanClassCreator magicDef = new MagicBeanClassCreator(
				"QueryBean-" + instance.getAndIncrement());

		this.resultSetExtractor = dialect.resultSetExtractorFor(resultSet);
		
		for (int i = 1; i <= resultSetExtractor.getColumnCount(); ++i) {
			
			magicDef.addProperty(
					resultSetExtractor.getColumnName(i), 
					resultSetExtractor.getColumnType(i));
		}
		
		this.arooaClass = magicDef.create();
	}
	
	public Object next() throws SQLException {
		if (!resultSetExtractor.next()) {
			return null;
		}
		
		Object bean = arooaClass.newInstance();
		
		BeanOverview overview = arooaClass.getBeanOverview(accessor);
		
		String[] properties = overview.getProperties();
		for (int i = 0; i < properties.length; ++i) {
			accessor.setProperty(bean, properties[i], 
					resultSetExtractor.getColumn(i + 1));
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
