package org.oddjob.framework.adapt.job;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Describable;
import org.oddjob.Forceable;
import org.oddjob.Iconic;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.framework.Transient;
import org.oddjob.framework.adapt.WrapperFactory;
import org.oddjob.logging.LogEnabled;

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
		interfaces.add(Resetable.class);
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
