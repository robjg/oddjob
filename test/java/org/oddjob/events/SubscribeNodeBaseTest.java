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

	private class OurSubscriber extends SubscribeNodeBase<Integer> {
		
		private Consumer<? super Integer> consumer;
		
		void publish(Integer i) {
			consumer.accept(i);
		}
		
		@Override
		protected Restore doStart(Consumer<? super Integer> consumer) {

			this.consumer = consumer;
			return () -> this.consumer = null;
		}
	};

	
	@Test
	public void testStatesStartPublishStop() throws Exception {
		
		
		Consumer<Number> consumer = v -> {};
		
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
		
		
		Consumer<Number> consumer = v -> {};
		
		SubscribeNodeBase<Integer> test = new SubscribeNodeBase<Integer>() {
			
			@Override
			protected Restore doStart(Consumer<? super Integer> consumer) {
				consumer.accept(2);
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
		
		
		Consumer<Number> consumer = v -> {};
		
		SubscribeNodeBase<Integer> test = new SubscribeNodeBase<Integer>() {
			
			@Override
			protected Restore doStart(Consumer<? super Integer> consumer) {
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
				
		Consumer<Number> consumer = v -> {};
		
		SubscribeNodeBase<Integer> test = new SubscribeNodeBase<Integer>() {
			
			@Override
			protected Restore doStart(Consumer<? super Integer> consumer) {
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
