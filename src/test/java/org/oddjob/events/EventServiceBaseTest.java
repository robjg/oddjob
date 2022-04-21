package org.oddjob.events;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.events.state.EventState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class EventServiceBaseTest {

	private static class OurSubscriber extends EventServiceBase<InstantEvent<Integer>> {
		
		private Consumer<? super InstantEvent<Integer>> consumer;
		
		void publish(Integer i) {
			consumer.accept(InstantEvent.of(i));
		}
		
		@Override
		protected Restore doStart(Consumer<? super InstantEvent<Integer>> consumer) {

			this.consumer = consumer;
			return () -> this.consumer = null;
		}
	}

	
	@Test
	public void testStatesStartPublishStop() throws Exception {
		
		
		Consumer<InstantEvent<Integer>> consumer = v -> {};
		
		OurSubscriber test = new OurSubscriber();
		
		StateSteps state = new StateSteps(test);
		state.startCheck(EventState.READY, EventState.CONNECTING, EventState.WAITING);

		test.setTo(consumer);
		test.run();

		state.checkNow();
		state.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);

		test.publish(2);
		
		state.checkNow();

		state.startCheck(EventState.TRIGGERED, EventState.FIRING, EventState.TRIGGERED);
		
		test.publish(4);
		
		state.checkNow();
		
		state.startCheck(EventState.TRIGGERED, EventState.COMPLETE);
		
		test.stop();
		
		state.checkNow();

		assertThat(test.consumer, nullValue());
	}
	
	@Test
	public void testStatesPublishStop() throws FailedToStopException {
		
		
		Consumer<InstantEvent<Integer>> consumer = v -> {};
		
		EventServiceBase<InstantEvent<Integer>> test =
				new EventServiceBase<InstantEvent<Integer>>() {
			@Override
			protected Restore doStart(Consumer<? super InstantEvent<Integer>> consumer) {
				consumer.accept(InstantEvent.of(2));
				return () -> {};
			}
		};
		
		StateSteps state = new StateSteps(test);
		state.startCheck(EventState.READY, EventState.CONNECTING, EventState.FIRING, EventState.TRIGGERED);

		test.setTo(consumer);
		test.run();

		state.checkNow();
		
		state.startCheck(EventState.TRIGGERED, EventState.COMPLETE);
		
		test.stop();
		
		state.checkNow();
	}
	
	@Test
	public void testStatesStop() throws FailedToStopException {
		
		
		Consumer<InstantEvent<Integer>> consumer = v -> {};
		
		EventServiceBase<InstantEvent<Integer>> test =
				new EventServiceBase<InstantEvent<Integer>>() {
			@Override
			protected Restore doStart(Consumer<? super InstantEvent<Integer>> consumer) {
				return () -> {};
			}
		};
		
		StateSteps state = new StateSteps(test);
		state.startCheck(EventState.READY, EventState.CONNECTING, EventState.WAITING);

		test.setTo(consumer);
		test.run();

		state.checkNow();
		
		state.startCheck(EventState.WAITING, EventState.INCOMPLETE);
		
		test.stop();
		
		state.checkNow();
	}

	@Test
	public void testStatesStartException() {
				
		Consumer<InstantEvent<Integer>> consumer = v -> {};
		
		EventServiceBase<InstantEvent<Integer>> test =
				new EventServiceBase<InstantEvent<Integer>>() {
			@Override
			protected Restore doStart(Consumer<? super InstantEvent<Integer>> consumer) {
				throw new RuntimeException("Doh!");
			}
		};
		
		StateSteps state = new StateSteps(test);
		state.startCheck(EventState.READY, EventState.CONNECTING, EventState.EXCEPTION);

		test.setTo(consumer);
		test.run();

		state.checkNow();
	}
}
