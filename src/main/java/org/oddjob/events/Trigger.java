package org.oddjob.events;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @oddjob.description Trigger on an event. This is a work in progress replacement
 * for {@link org.oddjob.scheduling.Trigger}. The intention being that it has the ability
 * to fire off any event, not just a state change.
 * <p>
 * The job has two children; the first being the source of the event that causes
 * the trigger, and the second is the job that is run as the result of the trigger
 * firing.
 * </p>
 *
 * @oddjob.example
 *
 * A trigger expression based on the state of some jobs.
 *
 * {@oddjob.xml.resource org/oddjob/events/TriggerExpressionExample.xml}
 *
 *
 * @author Rob Gordon.
 */
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

		logger().info("Will trigger off immediate event {}", value);
		eventRef.set(value);
	}
	
	@Override
	void onSubscriptionStarted(Object job, Executor executor) {
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

		logger().info("Running {} with Trigger Event {}", job, event);
		onStop();
		setTrigger(event);
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
