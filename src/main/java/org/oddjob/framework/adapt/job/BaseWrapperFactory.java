package org.oddjob.framework.adapt.job;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.*;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.framework.Transient;
import org.oddjob.framework.adapt.WrapperFactory;
import org.oddjob.logging.LogEnabled;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Shared class definitions for {@link Callable} and {@link Runnable}
 * wrappers.
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
		interfaces.add(ArooaSessionAware.class);
		interfaces.add(ArooaContextAware.class);
		interfaces.add(Stateful.class);
		interfaces.add(Resettable.class);
		interfaces.add(Forceable.class);
		interfaces.add(DynaBean.class);
		interfaces.add(Stoppable.class);
		interfaces.add(Iconic.class);
		interfaces.add(Runnable.class);
		interfaces.add(LogEnabled.class);
		interfaces.add(Describable.class);

		if (!(wrapped instanceof Serializable)) {
			interfaces.add(Transient.class);
		}

		return (Class[]) interfaces.toArray(new Class[0]);		
	}
	
}
