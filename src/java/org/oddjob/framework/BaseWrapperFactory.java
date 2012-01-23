package org.oddjob.framework;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Iconic;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.logging.LogEnabled;

/**
 * Shared class definitions for most wrappers.
 * 
 * @author rob
 *
 * @param <T> The {@link WrapperFactory} type.
 */
abstract public class BaseWrapperFactory<T> implements WrapperFactory<T> {

	@Override
	public Class<?>[] wrappingInterfacesFor(T wrapped) {
		
		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		interfaces.add(Object.class);
		interfaces.add(ArooaContextAware.class);
		interfaces.add(Stateful.class);
		interfaces.add(Resetable.class);
		interfaces.add(DynaBean.class);
		interfaces.add(Stoppable.class);
		interfaces.add(Iconic.class);
		interfaces.add(Runnable.class);
		interfaces.add(LogEnabled.class);

		if (!(wrapped instanceof Serializable)) {
			interfaces.add(Transient.class);
		}

		return (Class[]) interfaces.toArray(new Class[0]);		
	}
	
}
