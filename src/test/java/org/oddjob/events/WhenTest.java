package org.oddjob.events;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.Destination;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

class WhenTest {

	private static final Logger logger = LoggerFactory.getLogger(WhenTest.class);
	
	private static class OurSubscribable extends EventServiceBase<InstantEvent<Integer>> {

		volatile Consumer<? super InstantEvent<Integer>> consumer;
		
		@Override
		protected Restore doStart(Consumer<? super InstantEvent<Integer>> consumer) {
			this.consumer = consumer;
			return () -> this.consumer = null;
		}
		
		void next(int number) {
			consumer.accept(InstantEvent.of(number));
		}
	}

	static class OurResultsJob extends SimpleJob {

		final When<InstantEvent<Number>> test;

		final List<InstantEvent<Number>> results = new ArrayList<>();

		OurResultsJob(When<InstantEvent<Number>> test) {
            this.test = test;
        }

		protected int execute() {
			results.add(test.getTrigger());
			return 0;
		}

		@Override
		public String toString() {
			return "Our Job";
		}

    }

	@Test
	void whenStartedThenEventThenJobExecutedThenStop() throws FailedToStopException {
		
		OurSubscribable subscribe = new OurSubscribable();
				
		Queue<Runnable> executions = new LinkedList<>();
		ExecutorService executorService = mock(ExecutorService.class);
		
		Future<?> future = mock(Future.class);
		doAnswer(invocation -> {
			executions.add(invocation.getArgument(0, Runnable.class));
			return future;
			}).when(executorService).submit(Mockito.any(Runnable.class));

		When<InstantEvent<Number>> test = new When<>();

		OurResultsJob job = new OurResultsJob(test);
		
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
		subscribe.next(1);
		executions.remove().run();
		
		states.checkNow();
		states.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		logger.info("** Second Event");
		subscribe.next(2);
		executions.remove().run();

		states.checkNow();
		states.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		logger.info("** Third Event");
		subscribe.next(3);
		executions.remove().run();

		states.checkNow();

		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		logger.info("** Stopping");
		test.stop();
		
		states.checkNow();

		assertThat(job.results.get(0).getOf(), is(1));
		assertThat(job.results.get(1).getOf(), is(2));
		assertThat(job.results.get(2).getOf(), is(3));
		
		verify(future, times(0)).cancel(true);
	}
	
	@Test
	void whenStoppedThenUnsubscribedAndNoTriggered() throws FailedToStopException {
		
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

		assertThat(subscribe.consumer, nullValue());

		Mockito.verifyNoInteractions(executorService);
	}

	static class BadEventService extends EventServiceBase<Number> {

		@Override
		protected Restore doStart(Consumer<? super Number> consumer) throws Exception {
			throw new RuntimeException("I'm Bad");
		}
	}

	@Test
	void whenBadSourceThenWhenStops() {

		BadEventService subscribe = new BadEventService();

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
		states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.EXCEPTION);

		test.run();

		states.checkNow();

	}

	@Test
	void whenQueueStrategyThenAllExecuted() throws FailedToStopException {

		OurSubscribable subscribe = new OurSubscribable();

		Queue<Runnable> executions = new LinkedList<>();
		ExecutorService executorService = mock(ExecutorService.class);

		Future<?> future = mock(Future.class);
		doAnswer(invocation -> {
			executions.add(invocation.getArgument(0, Runnable.class));
			return future;
		}).when(executorService).submit(Mockito.any(Runnable.class));

		When<InstantEvent<Number>> test = new When<>();

		OurResultsJob job = new OurResultsJob(test);

		test.setJobs(0, subscribe);
		test.setJobs(1, job);
		test.setExecutorService(executorService);
		test.setTriggerStrategy(When.TriggerStrategies.QUEUE);

		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

		logger.info("** Starting queued");

		test.run();

		states.checkNow();

		states.startCheck(ParentState.ACTIVE, ParentState.STARTED,
				ParentState.ACTIVE, ParentState.STARTED);

		subscribe.next(1);
		subscribe.next(2);

		executions.remove().run();
		executions.remove().run();

		states.checkNow();

		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		logger.info("** Stopping queued");

		test.stop();

		states.checkNow();

		assertThat(job.results.get(0).getOf(), is(1));
		assertThat(job.results.get(1).getOf(), is(2));
	}

	@Test
	void noChildJob() {
		
		OurSubscribable subscribe = new OurSubscribable();
				
		ExecutorService executorService = mock(ExecutorService.class);
		
		When<InstantEvent<Number>> test = new When<>();

		test.setJobs(0, subscribe);
		test.setExecutorService(executorService);

		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.EXCEPTION);

		test.run();

		testStates.checkNow();

		assertThat(test.lastStateEvent().getException().getMessage(),
				containsString("A Job to run on receiving the event must be provided"));

		Mockito.verifyNoInteractions(executorService);
	}

	public static class PretendFileWatcher implements Runnable, AutoCloseable {

		private Consumer<? super String> consumer;

		@Stop
		@Override
		public void close() throws Exception {

		}

		@Start
		@Override
		public void run() {

		}

		@Destination
		public void setConsumer(Consumer<? super String> consumer) {
			this.consumer = consumer;
		}

		public void setSomeFileName(String someFileName) {
			this.consumer.accept(someFileName);
		}

		@Override
		public String toString() {
			return "PretendFileWatcher";
		}
	}

	public static class OnlyTxtFiles implements Predicate<String> {

		@Override
		public boolean test(String s) {
			return s.endsWith(".txt");
		}
	}

	@Test
	public void testWhenInBeanBus() throws ArooaConversionException, InterruptedException {

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/events/WhenAsDestinationExample.xml",
				getClass().getClassLoader()));

		StateSteps testStates = new StateSteps(oddjob);
		testStates.startCheck(ParentState.READY,
				ParentState.EXECUTING, ParentState.ACTIVE);

		oddjob.run();

		testStates.checkNow();

		OddjobLookup lookup = new OddjobLookup(oddjob);

		testStates.startCheck(ParentState.ACTIVE,
				ParentState.STARTED);

		lookup.lookup("set1", Runnable.class).run();

		testStates.checkWait();

		testStates.startCheck(
				ParentState.STARTED);

		assertThat(lookup.lookup("result.text", String.class), Matchers.is("Result: Fruit.txt"));

		lookup.lookup("set2", Runnable.class).run();

		testStates.checkNow();

		testStates.startCheck(
				ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		lookup.lookup("set3", Runnable.class).run();

		testStates.checkWait();

		assertThat(lookup.lookup("result.text", String.class), Matchers.is("Result: Prices.txt"));

		oddjob.destroy();
	}

}
