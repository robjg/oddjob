/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.events;
import static org.hamcrest.CoreMatchers.is;
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

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.Stateful;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TriggerTest extends OjTestCase {

	private static final Logger logger = 
		LoggerFactory.getLogger(TriggerTest.class);
	
    @Before
    public void setUp() throws Exception {

		logger.debug("----------------- " + getName() + " -------------");
	}

	private static class OurSubscribeable extends SubscribeNodeBase<Integer> {

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
	public void testStartJobExecutedStop() throws FailedToStopException {
		
		OurSubscribeable subscribe = new OurSubscribeable();
				
		Queue<Runnable> executions = new LinkedList<>();
		ExecutorService executorService = mock(ExecutorService.class);
		
		Future<?> future = mock(Future.class);
		doAnswer(invocation -> {
			executions.add(invocation.getArgumentAt(0, Runnable.class));
			return future;
			}).when(executorService).submit(Mockito.any(Runnable.class));

		Trigger<Number> test = new Trigger<Number>(); 

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
		test.initialise();
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.run();

		states.checkNow();

		states.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);

		subscribe.next();
		executions.remove().run();
		
		states.checkNow();

		assertThat(subscribe.consumer, CoreMatchers.nullValue());

		assertThat(results.get(0), is(1));
		assertThat(results.size(), is(1));
		
		verify(future, times(0)).cancel(true);
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
 		
 		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());

 		testStates.checkNow();

 		testStates.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
 		
 		Runnable thing1 = (Runnable) lookup.lookup("thing1");
 		thing1.run();
 		Runnable thing2 = (Runnable) lookup.lookup("thing2");
 		thing2.run();
 		
 		testStates.checkWait();

 		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
 		
 		oddjob.destroy();
 	}
    
    
}
