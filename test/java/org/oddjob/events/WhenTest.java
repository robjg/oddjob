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
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;

public class WhenTest {

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

		When<Number> test = new When<Number>(); 

		List<Object> results = new ArrayList<>();

		SimpleJob job = new SimpleJob() {

			protected int execute() throws Throwable {
				results.add(test.getCurrent());
				return 0;
			}
		};
		
		test.setJobs(0, subscribe);
		test.setJobs(1, job);
		test.setExecutorService(executorService);
		
		StateSteps states = new StateSteps(test);
		states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.run();

		states.checkNow();

		states.startCheck(ParentState.ACTIVE, ParentState.STARTED);

		subscribe.next();
		executions.remove().run();
		
		states.checkNow();
		states.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		subscribe.next();
		executions.remove().run();

		states.checkNow();
		states.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

		subscribe.next();
		executions.remove().run();

		states.checkNow();

		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);

		test.stop();
		
		states.checkNow();

		assertThat(results.get(0), is(1));
		assertThat(results.get(1), is(2));
		assertThat(results.get(2), is(3));
		
		verify(future, times(0)).cancel(true);
	}
	
}
