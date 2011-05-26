package org.oddjob.jmx;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.jmx.handlers.VanillaServerHandlerFactory;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;

public class VanillaInterfaceHandler<T> 
implements ValueFactory<ServerInterfaceHandlerFactory<T, T>>, ArooaSessionAware {

	
	private ArooaSession session;
	
	private String className;

	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@SuppressWarnings("unchecked")
	public ServerInterfaceHandlerFactory<T, T> toValue() {
		Class<T> cl = (Class<T>) session.getArooaDescriptor().getClassResolver().findClass(className);
		return new VanillaServerHandlerFactory<T>(cl);
	}
}
