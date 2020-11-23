package org.oddjob.framework.adapt;

import org.oddjob.Resettable;
import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.utils.AnnotationFinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Create an adaptor to an {@link Resettable} that adapts a component either
 * because it is {@link Resettable} or because it has annotations to resetable
 * methods. 
 * 
 * @author rob
 *
 */
public class ResetableAdaptorFactory implements AdaptorFactory<Resettable> {

	/**
	 * Create a resetable.
	 *
	 * @param component
	 * @param session
	 *
	 * @return Possibly a Resetable.
	 */
	@Override
	public Optional<Resettable> adapt(Object component, ArooaSession session) {

		if (component instanceof Resettable) {
			return Optional.of((Resettable) component);
		}

		ArooaAnnotations annotations = AnnotationFinder.forSession(session)
				.findFor(component.getClass());

		final Method softResetMethod = 
				annotations.methodFor(SoftReset.class.getName());
		final Method hardResetMethod = 
				annotations.methodFor(HardReset.class.getName());

		if (softResetMethod == null && hardResetMethod == null) {
			return Optional.empty();
		}


		return Optional.of(new Resettable() {
			
			@Override
			public boolean softReset() {
				invoke(component, softResetMethod);
				return true;
			}
			
			@Override
			public boolean hardReset() {
				invoke(component, hardResetMethod);
				return true;
			}
		});
	}
	
	private void invoke(Object component, Method m) {
		if (m == null) {
			return;
		}
		try {
			m.invoke(component);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
