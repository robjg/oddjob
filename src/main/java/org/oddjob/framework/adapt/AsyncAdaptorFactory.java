package org.oddjob.framework.adapt;

import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.utils.AnnotationFinder;
import org.oddjob.framework.AsyncJob;
import org.oddjob.util.OddjobWrapperException;

import java.beans.ExceptionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
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

		if (component instanceof Callable
				&& CompletableFuture.class.isAssignableFrom(getCallableType((Callable<?>) component))) {

			//noinspection unchecked
			return Optional.of(new AsyncCallableAdaptor((Callable<CompletableFuture<?>>) component,
					session.getTools().getArooaConverter()));
		}

		if (!(component instanceof Runnable)) {
			// should this just be an exception?
			return Optional.empty();
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
			public void run() {
				((Runnable) component).run();
									}

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

	static class AsyncCallableAdaptor implements AsyncJob {

		private final Callable<CompletableFuture<?>> callable;

		private final ArooaConverter converter;

		private IntConsumer stopWithState;

		private ExceptionListener exceptionListener;

		AsyncCallableAdaptor(Callable<CompletableFuture<?>> callable, ArooaConverter converter) {
			this.callable = callable;
			this.converter = converter;
		}

		@Override
		public void run() {
			try {
				CompletableFuture<?> completableFuture = callable.call();

				completableFuture.whenComplete((v, t) -> {
					if (t == null) {
						try {
							stopWithState.accept(converter.convert(v, Integer.class));
						}
						catch (ArooaConversionException e) {
							exceptionListener.exceptionThrown(e);
						}
					}
					else {
						exceptionListener.exceptionThrown((Exception) t);
					}
				});
			} catch (Exception e) {
				exceptionListener.exceptionThrown(e);
			}
		}

		@Override
		public void acceptCompletionHandle(IntConsumer stopWithState) {
			this.stopWithState = stopWithState;
		}

		@Override
		public void acceptExceptionListener(ExceptionListener exceptionListener) {
			this.exceptionListener = exceptionListener;
		}
	}

	static <T> Class<T> getCallableType(Callable<T> callable) {

		return getCallableTypeOf(callable.getClass());
	}

	@SuppressWarnings("rawtypes")
	static <T> Class<T> getCallableTypeOf(Class<? extends Callable> callableClass) {

		Optional<Type> callableInterfaceType = Arrays.stream(callableClass.getGenericInterfaces())
				.filter(c -> Callable.class == rawType(c))
				.findFirst();

		if (callableInterfaceType.isPresent()) {

			Type t = ((ParameterizedType) callableInterfaceType.get()).getActualTypeArguments()[0];

			//noinspection unchecked
			return (Class<T>) rawType(t);
		}
		else {

			return getCallableTypeOf((Class<Callable>)callableClass.getSuperclass());
		}
	}

	static Class<?> rawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		else if (type instanceof ParameterizedType){
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		else {
			throw new IllegalArgumentException("Can't work out raw type of [" + type + "]");
		}
	}
}
