package org.oddjob.sql;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.mega.MegaBeanBus;

import java.io.Flushable;
import java.io.IOException;
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

	@Override
	public void flush() throws IOException {
		if (to instanceof Flushable) {
			((Flushable) to).flush();
		}
	}

	@Override
	public void run() {
		if (to instanceof Runnable) {
			((Runnable) to).run();
		}
	}

	@Override
	public void close() throws Exception {
		super.close();
		if (to instanceof AutoCloseable) {
			((AutoCloseable) to).close();
		}
	}

	@Override
	public String toString() {
		return "SQLResultsBus{" +
				"to=" + to +
				'}';
	}
}
