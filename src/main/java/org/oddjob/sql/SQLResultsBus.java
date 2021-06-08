package org.oddjob.sql;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.mega.MegaBeanBus;

import java.util.function.Consumer;

/**
 * @oddjob.description 
 * 
 * A {@link SQLResultHandler} that attaches to 
 * {@link BeanBus} components.
 * 
 * @oddjob.example 
 * 
 * Writing to a list.
 * 
 * {@oddjob.xml.resource org/oddjob/sql/SQLResultsBusExample.xml}
 * 
 * @oddjob.example 
 * 
 * Within a {@link MegaBeanBus}.
 * 
 * {@oddjob.xml.resource org/oddjob/sql/SQLResultsBusExample2.xml}
 * 
 * @author rob
 */
public class SQLResultsBus extends BeanFactoryResultHandler {
	
	private final Consumer<? super Object> to;
	
	public SQLResultsBus(Consumer<? super Object> to,
			ArooaSession session) {
		super(session);
		this.to = to;
	}
	
	@Override
	protected void accept(Object bean) {
				
		if (to != null) {
			to.accept(bean);
		}
	}
}
