package org.oddjob.sql;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.reflect.PropertyAccessor;

import java.io.Flushable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A {@link SQLResultHandler} that creates beans.
 * 
 * @author rob
 *
 */
abstract public class BeanFactoryResultHandler 
implements SQLResultHandler, AutoCloseable, Runnable, Flushable {

	private final PropertyAccessor accessor;
	
	private volatile boolean stop;
	
	public BeanFactoryResultHandler(ArooaSession session) {
		this.accessor = session.getTools(
				).getPropertyAccessor().accessorWithConversions(
						session.getTools().getArooaConverter());
	}
		
	@Override
	public final void handleResultSet(ResultSet resultSet, 
			DatabaseDialect dialect) throws SQLException, ClassNotFoundException {
		
		if (accessor == null) {
			throw new NullPointerException(
					"No Property Accessor. Was setArooaSession Called?");
		}
		
		ResultSetBeanFactory beanFactory = new ResultSetBeanFactory(
				resultSet, accessor, 
				dialect == null ? 
						new BasicGenericDialect() : dialect);

		for (Object next = beanFactory.next(); !stop && next != null; 
				next = beanFactory.next()) {
			accept(next);
		}
	}
	
	@Override
	public final void handleUpdate(int updateCount,
			DatabaseDialect dialect) {
		accept(new UpdateCount(updateCount));
	}
	
	abstract protected void accept(Object bean);

	@Override
	public void close() throws Exception {
		stop = true;
	}
}
