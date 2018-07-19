package org.oddjob.events;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Trigger<T> extends EventJobBase<T> {

	private static final long serialVersionUID = 2018071300L; 

	private final AtomicBoolean ran = new AtomicBoolean();

	private final AtomicReference<T> eventRef = new AtomicReference<>();
	
	@Override
	protected void onReset() {
		ran.set(false);
		eventRef.set(null);
		super.onReset();
	}

	@Override
	void onImmediateEvent(T value) {
		if (ran.getAndSet(true)) {
			return;
		}
		eventRef.set(value);
		
	}
	
	@Override
	void onSubscriptonStarted(Object job, Executor executor) {
		Optional.ofNullable(eventRef.get()).ifPresent(
				e -> trigger(e, job, executor));
	}

	@Override
	void onLaterEvent(T value, Object job, Executor executor) {
		if (ran.getAndSet(true)) {
			return;
		}		
		trigger( value, job, executor);
	}
	
	private void trigger(T event, Object job, Executor executor) {
		
		onStop();
		setCurrent(event);
		if (job != null) {
			if (job instanceof Runnable) {
				executor.execute((Runnable) job);
			}
		}
	}
	
	public AtomicBoolean getRan() {
		return ran;
	}
}
