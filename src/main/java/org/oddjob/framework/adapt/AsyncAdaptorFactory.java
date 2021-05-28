package org.oddjob.framework.adapt;

import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.utils.AnnotationFinder;
import org.oddjob.framework.AsyncJob;
import org.oddjob.util.OddjobWrapperException;

import java.beans.ExceptionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.IntConsumer;

/**
 * Create an adaptor to a {@link AsyncJob} that adapts a component either
 * because it is an {@link AsyncJob} or because it has annotated
 * methods. 
 * 
 * @author rob
 *
 */
public class AsyncAdaptorFactory implements AdaptorFactory<AsyncJob> {

	/**
	 * Possibly create an {@link AsyncJob}.
	 *
	 * @param component
	 * @param session
	 *
	 * @return Possibly a AsyncJob.
	 */
	@Override
	public Optional<AsyncJob> adapt(Object component, ArooaSession session) {

		if (component instanceof AsyncJob) {
			return Optional.of((AsyncJob) component);
		}

		ArooaAnnotations annotations = AnnotationFinder.forSession(session)
				.findFor(component.getClass());

		final Method acceptCompletionHandle =
				annotations.methodFor(AcceptCompletionHandle.class.getName());
		final Method acceptExceptionListener =
				annotations.methodFor(AcceptExceptionListener.class.getName());

		if (acceptCompletionHandle == null && acceptExceptionListener == null) {
			return Optional.empty();
		}

		return Optional.of(new AsyncJob() {

			@Override
			public void acceptCompletionHandle(IntConsumer stopWithState) {
				invoke(component, acceptCompletionHandle, stopWithState);
			}

			@Override
			public void acceptExceptionListener(ExceptionListener exceptionListener) {
				invoke(component, acceptExceptionListener, exceptionListener);
			}

		});
	}
	
	private void invoke(Object component, Method m, Object arg) {
		if (m == null) {
			return;
		}

		try {
			m.invoke(component, arg);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new OddjobWrapperException(e);
		}
	}
	
}
