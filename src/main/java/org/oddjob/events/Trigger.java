package org.oddjob.events;

import org.oddjob.state.StateConditions;
import org.oddjob.state.StateListener;

import java.util.Optional;
import java.util.concurrent.Executor;
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
 * @oddjob.example
 *
 * A trigger as a destination in Bean Bus. The queue is required to keep the bus open while the triggered
 * job completes. Using just a bus driver would cause the bus to be closed when the driver completes and this
 * might not give time for the triggered job to complete because it happens asynchronously. The solution is to
 * make Trigger flushable and not let flush complete until the triggered job completes.
 *
 * {@oddjob.xml.resource org/oddjob/events/TriggerAsDestinationExample.xml}
 *
 * @oddjob.example
 *
 * Trigger with the first result of a Bean Bus pipeline.
 *
 * {@oddjob.xml.resource org/oddjob/events/TriggerBusDriver.xml}
 *
 * @author Rob Gordon.
 */
public class Trigger<T> extends EventJobBase<T> {

	private static final long serialVersionUID = 2018071300L; 

	/** The first event received. */
	private final AtomicReference<T> eventRef = new AtomicReference<>();
	
	@Override
	protected void onReset() {
		eventRef.set(null);
		super.onReset();
	}

	@Override
	protected void onImmediateEvent(T value) {

		if (eventRef.compareAndSet(null, value)) {
			logger().info("Will trigger off immediate event {}", value);
		}
	}
	
	@Override
	protected void onSubscriptionStarted(Object job, Executor executor) {
		Optional.ofNullable(eventRef.get()).ifPresent(
				e -> trigger(e, job, executor));
	}

	@Override
	protected void onLaterEvent(T value, Object job, Executor executor) {
		if (eventRef.compareAndSet(null, value)) {
			trigger( value, job, executor);
		}
		else {
			logger().info("Ignoring event {}", value);
		}
	}
	
	private void trigger(T event, Object job, Executor executor) {

		logger().info("Running {} with Trigger Event {}", job, event);
		unsubscribe();
		setTrigger(event);
		if (job instanceof Runnable) {
			executor.execute((Runnable) job);
		}
	}

	@Override
	protected StateListener stateOnChildComplete() {
		return event -> {
			if (StateConditions.FINISHED.test(event.getState())) {
				unsubscribe();
				switchToChildStateReflector();
			}
		};
	}
}
