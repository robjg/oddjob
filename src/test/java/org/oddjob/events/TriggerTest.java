/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.events;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

/**
 * 
 */
public class TriggerTest {

	private static final Logger logger = 
		LoggerFactory.getLogger(TriggerTest.class);

	@Rule
	public TestName name = new TestName();

	public String getName() {
		return name.getMethodName();
	}

	@Before
    public void setUp() throws Exception {

		logger.debug("----------------- " + getName() + " -------------");
	}

	private static class OurSubscribable extends EventServiceBase<InstantEvent<Integer>> {

		final List<Integer> ints = Arrays.asList(1, 2, 3);
		
		final AtomicInteger index = new AtomicInteger();
		
		volatile Consumer<? super InstantEvent<Integer>> consumer;
		
		@Override
		protected Restore doStart(Consumer<? super InstantEvent<Integer>> consumer) {
			this.consumer = consumer;
			return () -> this.consumer = null;
		}
		
		void next() {
			Objects.requireNonNull(consumer);
			consumer.accept(new WrapperOf<>(ints.get(index.getAndIncrement()),
					Instant.now()));
		}
	}

	@Test
	public void testStartJobExecutedStop() {
		
		OurSubscribable subscribe = new OurSubscribable();
				
		Queue<Runnable> executions = new LinkedList<>();
		ExecutorService executorService = mock(ExecutorService.class);
		
		Future<?> future = mock(Future.class);
		doAnswer(invocation -> {
			executions.add(invocation.getArgument(0, Runnable.class));
			return future;
			}).when(executorService).submit(Mockito.any(Runnable.class));

		Trigger<InstantEvent<Number>> test = new Trigger<>();

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
		test.initialise();
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.run();

		states.checkNow();

		states.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);

		subscribe.next();
		executions.remove().run();
		
		states.checkNow();

		assertThat(subscribe.consumer, nullValue());

		assertThat(results.get(0).getOf(), is(1));
		assertThat(results.size(), is(1));
		
		verify(future, times(0)).cancel(true);
	}

	@Test
	public void testStopUntriggered() throws FailedToStopException {
		
		OurSubscribable subscribe = new OurSubscribable();
		
		ExecutorService executorService = mock(ExecutorService.class);
		
		Trigger<Number> test = new Trigger<>();

		SimpleJob job = new SimpleJob() {

			protected int execute() {
				throw new RuntimeException("Unexpected");
			}
		};
		
		test.setJobs(0, subscribe);
		test.setJobs(1, job);
		test.setExecutorService(executorService);
		test.initialise();
		
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
	public void testNoChildJob() {
		
		OurSubscribable subscribe = new OurSubscribable();
				
		ExecutorService executorService = mock(ExecutorService.class);
		
		Trigger<InstantEvent<Number>> test = new Trigger<>();

		test.setJobs(0, subscribe);
		test.setExecutorService(executorService);
		test.initialise();
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.EXCEPTION);

		test.run();

		testStates.checkNow();

		assertThat(test.lastStateEvent().getException().getMessage(),
				containsString("A Job to run on receiving the event must be provided"));

		Mockito.verifyNoInteractions(executorService);
	}
	
	private static class SendWhenConnecting extends EventServiceBase<InstantEvent<Integer>> {
		
		@Override
		protected Restore doStart(Consumer<? super InstantEvent<Integer>> consumer) {
			consumer.accept(new WrapperOf<>(42, Instant.now()));
			return () -> {};
		}
		
	}

	@Test
	public void testJobExecutedQuickly() {
		
		ExecutorService executorService = mock(ExecutorService.class);
		
		Future<?> future = mock(Future.class);
		doAnswer(invocation -> {
			invocation.getArgument(0, Runnable.class).run();
			return future;
			}).when(executorService).submit(Mockito.any(Runnable.class));

		Trigger<InstantEvent<Number>> test = new Trigger<>();

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
		
		test.setJobs(0, new SendWhenConnecting());
		test.setJobs(1, job);
		test.setExecutorService(executorService);
		test.initialise();
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, 
				ParentState.EXECUTING, 
				ParentState.ACTIVE, 
				ParentState.COMPLETE);
		
		test.run();

		states.checkNow();

		assertThat(results.get(0).getOf(), is(42));
		assertThat(results.size(), is(1));
		
		verify(future, times(0)).cancel(true);
	}

	@Test
	public void testSimpleStateExpressionAsPropertyExample() throws InterruptedException {

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/events/TriggerStateExpressionPropertyExample.xml",
				getClass().getClassLoader()));

		oddjob.load();

		assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));

		OddjobLookup lookup = new OddjobLookup(oddjob);

		Stateful test = (Stateful) lookup.lookup("trigger");

		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(ParentState.READY,
				ParentState.EXECUTING, ParentState.ACTIVE);

		oddjob.run();

		assertThat(oddjob.lastStateEvent().getState(),
				is(ParentState.ACTIVE));

		testStates.checkNow();

		testStates.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);

		Runnable thing1 = (Runnable) lookup.lookup("job1");
		thing1.run();
		Runnable thing2 = (Runnable) lookup.lookup("job2");
		thing2.run();

		testStates.checkWait();

		assertThat(oddjob.lastStateEvent().getState(),
				is(ParentState.COMPLETE));

		oddjob.destroy();
	}
	
    @Test
 	public void testExpressionExample() throws InterruptedException {
 		
 		Oddjob oddjob = new Oddjob();
 		oddjob.setConfiguration(new XMLConfiguration(
 				"org/oddjob/events/TriggerExpressionExample.xml",
 				getClass().getClassLoader()));
 				
 		oddjob.load();

 		assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));
 		
 		OddjobLookup lookup = new OddjobLookup(oddjob);
 		
 		Stateful test = (Stateful) lookup.lookup("trigger");
 		
 		StateSteps testStates = new StateSteps(test);
 		testStates.startCheck(ParentState.READY, 
 				ParentState.EXECUTING, ParentState.ACTIVE);
 		
 		oddjob.run();
 		
 		assertThat(oddjob.lastStateEvent().getState(),
				is(ParentState.ACTIVE));

 		testStates.checkNow();

 		testStates.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
 		
 		Runnable thing1 = (Runnable) lookup.lookup("thing1");
 		thing1.run();
 		Runnable thing2 = (Runnable) lookup.lookup("thing2");
 		thing2.run();
 		
 		testStates.checkWait();

		assertThat(oddjob.lastStateEvent().getState(),
				is(ParentState.COMPLETE));
 		
 		oddjob.destroy();
 	}

 	@Test
 	public void testEventSourceAsPropertyIsClosed() {

		AtomicReference<Consumer<? super String>> consumerRef = new AtomicReference<>();

		AtomicBoolean stopped = new AtomicBoolean();

		EventSource<String> eventSource = consumer -> {
			consumerRef.set(consumer);
			return () -> stopped.set(true);
		};

		FlagState flagState = new FlagState();

		ExecutorService executorService = mock(ExecutorService.class);

		Trigger<String> test = new Trigger<>();
		test.setEventSource(eventSource);
		test.setJobs(0, flagState);
		test.setExecutorService(executorService);

		test.run();

		assertThat(test.lastStateEvent().getState(), is(ParentState.ACTIVE));

		consumerRef.get().accept("Foo");

		ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
		verify(executorService, times(1)).submit(captor.capture());

		captor.getValue().run();

		assertThat(test.lastStateEvent().getState(), is(ParentState.COMPLETE));
		assertThat(flagState.lastStateEvent().getState(), is(JobState.COMPLETE));
		assertThat(stopped.get(), is(true));
	}

	@Test
	public void testWithBusDriver() throws InterruptedException {

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/events/TriggerBusDriver.xml",
				getClass().getClassLoader()));

		StateSteps testStates = new StateSteps(oddjob);
		testStates.startCheck(ParentState.READY,
				ParentState.EXECUTING, ParentState.ACTIVE, ParentState.COMPLETE);

		oddjob.run();

		testStates.checkWait();

		oddjob.destroy();
	}

	@Test
	public void testTriggerInBeanBus() throws ArooaConversionException, InterruptedException {

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/events/TriggerAsDestinationExample.xml",
				getClass().getClassLoader()));

		StateSteps testStates = new StateSteps(oddjob);
		testStates.startCheck(ParentState.READY,
				ParentState.EXECUTING, ParentState.COMPLETE);

		oddjob.run();

		testStates.checkWait();

		String result = new OddjobLookup(oddjob).lookup("result.text", String.class);

		assertThat(result, Matchers.is("Result: 3"));

		oddjob.destroy();
	}
}
