package org.oddjob.jmx;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.jmx.handlers.VanillaServerHandlerFactory;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;

/**
 * Provide a generic {@link org.oddjob.jmx.server.ServerInterfaceManagerFactory} for an interface.
 *
 * @param <T> The type the handler is for.
 */
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
	public ServerInterfaceHandlerFactory<T, T> toValue() throws ArooaConversionException {
		if (className == null) {
			throw new ArooaConversionException("No class name." );
		}
		Class<T> cl = (Class<T>) session.getArooaDescriptor().getClassResolver().findClass(className);
		if (cl == null) {
			throw new ArooaConversionException("Failed to find class " + className);
		}
		return new VanillaServerHandlerFactory<>(cl);
	}
}
