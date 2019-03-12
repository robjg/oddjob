package org.oddjob.events;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;
import org.oddjob.events.state.EventState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;

public class SubscribeNodeBaseTest {

	private class OurSubscriber extends EventSourceBase<Integer> {
		
		private Consumer<? super EventOf<Integer>> consumer;
		
		void publish(Integer i) {
			consumer.accept(EventOf.of(i));
		}
		
		@Override
		protected Restore doStart(Consumer<? super EventOf<Integer>> consumer) {

			this.consumer = consumer;
			return () -> this.consumer = null;
		}
	}

	
	@Test
	public void testStatesStartPublishStop() throws Exception {
		
		
		Consumer<EventOf<Integer>> consumer = v -> {};
		
		OurSubscriber test = new OurSubscriber();
		
		StateSteps state = new StateSteps(test);
		state.startCheck(EventState.READY, EventState.CONNECTING, EventState.WAITING);

		Restore close = test.start(consumer);
		
		state.checkNow();
		state.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);

		test.publish(2);
		
		state.checkNow();

		state.startCheck(EventState.TRIGGERED, EventState.FIRING, EventState.TRIGGERED);
		
		test.publish(4);
		
		state.checkNow();
		
		state.startCheck(EventState.TRIGGERED, EventState.COMPLETE);
		
		close.close();
		
		state.checkNow();

		assertThat(test.consumer, nullValue());
	}
	
	@Test
	public void testStatesPublishStop() throws Exception {
		
		
		Consumer<EventOf<Integer>> consumer = v -> {};
		
		EventSourceBase<Integer> test = new EventSourceBase<Integer>() {
			
			@Override
			protected Restore doStart(Consumer<? super EventOf<Integer>> consumer) {
				consumer.accept(EventOf.of(2));
				return () -> {};
			}
		};
		
		StateSteps state = new StateSteps(test);
		state.startCheck(EventState.READY, EventState.CONNECTING, EventState.FIRING, EventState.TRIGGERED);

		Restore close = test.start(consumer);
		
		state.checkNow();
		
		state.startCheck(EventState.TRIGGERED, EventState.COMPLETE);
		
		close.close();
		
		state.checkNow();
	}
	
	@Test
	public void testStatesStop() throws Exception {
		
		
		Consumer<EventOf<Integer>> consumer = v -> {};
		
		EventSourceBase<Integer> test = new EventSourceBase<Integer>() {
			
			@Override
			protected Restore doStart(Consumer<? super EventOf<Integer>> consumer) {
				return () -> {};
			}
		};
		
		StateSteps state = new StateSteps(test);
		state.startCheck(EventState.READY, EventState.CONNECTING, EventState.WAITING);

		Restore close = test.start(consumer);
		
		state.checkNow();
		
		state.startCheck(EventState.WAITING, EventState.INCOMPLETE);
		
		close.close();
		
		state.checkNow();
	}

	@Test
	public void testStatesStartException() throws Exception {
				
		Consumer<EventOf<Integer>> consumer = v -> {};
		
		EventSourceBase<Integer> test = new EventSourceBase<Integer>() {
			
			@Override
			protected Restore doStart(Consumer<? super EventOf<Integer>> consumer) {
				throw new RuntimeException("Doh!");
			}
		};
		
		StateSteps state = new StateSteps(test);
		state.startCheck(EventState.READY, EventState.CONNECTING, EventState.EXCEPTION);

		try {
			test.start(consumer);
			Assert.fail("Should fail.");
		}
		catch (RuntimeException e) {
			// expected
		}
		
		state.checkNow();
	}
}
