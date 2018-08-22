package org.oddjob.events;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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

public class WhenTest {

	private static final Logger logger = LoggerFactory.getLogger(WhenTest.class);
	
	private static class OurSubscribeable extends EventSourceBase<Integer> {

		final List<Integer> ints = Arrays.asList(1, 2, 3);
		
		final AtomicInteger index = new AtomicInteger();
		
		volatile Consumer<? super Integer> consumer;
		
		@Override
		protected Restore doStart(Consumer<? super Integer> consumer) {
			this.consumer = consumer;
			return () -> this.consumer = null;
		}
		
		void next() {
			consumer.accept(ints.get(index.getAndIncrement()));
		}
	}; 
	
	@Test
	public void testStartedThenEventThenJobExecutedThenStop() throws FailedToStopException {
		
		OurSubscribeable subscribe = new OurSubscribeable();
				
		Queue<Runnable> executions = new LinkedList<>();
		ExecutorService executorService = mock(ExecutorService.class);
		
		Future<?> future = mock(Future.class);
		doAnswer(invocation -> {
			executions.add(invocation.getArgumentAt(0, Runnable.class));
			return future;
			}).when(executorService).submit(Mockito.any(Runnable.class));

		When<Number> test = new When<Number>(); 

		List<Object> results = new ArrayList<>();

		SimpleJob job = new SimpleJob() {

			protected int execute() throws Throwable {
				results.add(test.getCurrent());
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

		assertThat(results.get(0), is(1));
		assertThat(results.get(1), is(2));
		assertThat(results.get(2), is(3));
		
		verify(future, times(0)).cancel(true);
	}
	
	@Test
	public void testStopUntriggered() throws FailedToStopException {
		
		OurSubscribeable subscribe = new OurSubscribeable();
		
		ExecutorService executorService = mock(ExecutorService.class);
		
		When<Number> test = new When<Number>(); 

		SimpleJob job = new SimpleJob() {

			protected int execute() throws Throwable {
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

		Mockito.verifyZeroInteractions(executorService);
	}
	
	@Test
	public void testNoChildJob() throws FailedToStopException {
		
		OurSubscribeable subscribe = new OurSubscribeable();
				
		ExecutorService executorService = mock(ExecutorService.class);
		
		When<Number> test = new When<Number>(); 

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
		
		assertThat(test.getCurrent(), is(1));
		
		subscribeStates.checkNow();
		testStates.checkNow();
		
		testStates.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		subscribe.next();

		assertThat(test.getCurrent(), is(2));

		testStates.checkNow();
		testStates.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		subscribe.next();

		assertThat(test.getCurrent(), is(3));

		testStates.checkNow();

		testStates.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		test.stop();
		
		testStates.checkNow();

		Mockito.verifyZeroInteractions(executorService);
	}
	
}
