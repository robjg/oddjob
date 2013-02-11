package org.oddjob.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.PropertyAccessor;

/**
 * A {@link SQLResultHandler} that creates beans.
 * 
 * @author rob
 *
 */
abstract public class BeanFactoryResultHandler 
implements SQLResultHandler, ArooaSessionAware {

	private PropertyAccessor accessor;
	
	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
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

		for (Object next = beanFactory.next(); next != null; 
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
	
}
