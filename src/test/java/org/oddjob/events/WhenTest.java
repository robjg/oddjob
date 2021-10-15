package org.oddjob.events;

import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.FailedToStopException;
import org.oddjob.events.state.EventState;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class WhenTest {

	private static final Logger logger = LoggerFactory.getLogger(WhenTest.class);
	
	private static class OurSubscribable extends InstantEventSourceBase<Integer> {

		final List<InstantEvent<Integer>> ints = Arrays.asList(
				InstantEvent.of(1), InstantEvent.of(2), InstantEvent.of(3));
		
		final AtomicInteger index = new AtomicInteger();
		
		volatile Consumer<? super InstantEvent<Integer>> consumer;
		
		@Override
		protected Restore doStart(Consumer<? super InstantEvent<Integer>> consumer) {
			this.consumer = consumer;
			return () -> this.consumer = null;
		}
		
		void next() {
			consumer.accept(ints.get(index.getAndIncrement()));
		}
	}

	@Test
	public void testStartedThenEventThenJobExecutedThenStop() throws FailedToStopException {
		
		OurSubscribable subscribe = new OurSubscribable();
				
		Queue<Runnable> executions = new LinkedList<>();
		ExecutorService executorService = mock(ExecutorService.class);
		
		Future<?> future = mock(Future.class);
		doAnswer(invocation -> {
			executions.add(invocation.getArgument(0, Runnable.class));
			return future;
			}).when(executorService).submit(Mockito.any(Runnable.class));

		When<InstantEvent<Number>> test = new When<>();

		List<InstantEvent<Number>> results = new ArrayList<>();

		SimpleJob job = new SimpleJob() {

			protected int execute() {
				results.add(test.getTrigger());
				return 0;
			}
			
			@Override
			public String toString() {
				return "Our Job";
			}
		};
		
		test.setJobs(0, subscribe);
		test.setJobs(1, job);
		test.setExecutorService(executorService);
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);
		
		logger.info("** Starting");
		test.run();

		states.checkNow();

		states.startCheck(ParentState.ACTIVE, ParentState.STARTED);

		logger.info("** First Event");
		subscribe.next();
		executions.remove().run();
		
		states.checkNow();
		states.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		logger.info("** Second Event");
		subscribe.next();
		executions.remove().run();

		states.checkNow();
		states.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		logger.info("** Third Event");
		subscribe.next();
		executions.remove().run();

		states.checkNow();

		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		logger.info("** Stopping");
		test.stop();
		
		states.checkNow();

		assertThat(results.get(0).getOf(), is(1));
		assertThat(results.get(1).getOf(), is(2));
		assertThat(results.get(2).getOf(), is(3));
		
		verify(future, times(0)).cancel(true);
	}
	
	@Test
	public void testStopUntriggered() throws FailedToStopException {
		
		OurSubscribable subscribe = new OurSubscribable();
		
		ExecutorService executorService = mock(ExecutorService.class);
		
		When<Number> test = new When<>();

		SimpleJob job = new SimpleJob() {

			protected int execute() {
				throw new RuntimeException("Unexpected");
			}
		};
		
		test.setJobs(0, subscribe);
		test.setJobs(1, job);
		test.setExecutorService(executorService);
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.run();

		states.checkNow();

		states.startCheck(ParentState.ACTIVE, ParentState.INCOMPLETE);

		test.stop();
		
		states.checkNow();

		Mockito.verifyNoInteractions(executorService);
	}
	
	@Test
	public void testNoChildJob() throws FailedToStopException {
		
		OurSubscribable subscribe = new OurSubscribable();
				
		ExecutorService executorService = mock(ExecutorService.class);
		
		When<InstantEvent<Number>> test = new When<>();

		test.setJobs(0, subscribe);
		test.setExecutorService(executorService);
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);
		StateSteps subscribeStates = new StateSteps(subscribe);
		subscribeStates.startCheck(EventState.READY, EventState.CONNECTING, EventState.WAITING);
		
		test.run();

		subscribeStates.checkNow();
		testStates.checkNow();

		testStates.startCheck(ParentState.ACTIVE, ParentState.STARTED);
		subscribeStates.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);
		subscribe.next();
		
		assertThat(test.getTrigger().getOf(), is(1));
		
		subscribeStates.checkNow();
		testStates.checkNow();
		
		testStates.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		subscribe.next();

		assertThat(test.getTrigger().getOf(), is(2));

		testStates.checkNow();
		testStates.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		subscribe.next();

		assertThat(test.getTrigger().getOf(), is(3));

		testStates.checkNow();

		testStates.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		test.stop();
		
		testStates.checkNow();

		Mockito.verifyNoInteractions(executorService);
	}
	
}
