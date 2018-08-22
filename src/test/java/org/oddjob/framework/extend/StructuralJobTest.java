/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework.extend;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.OjTestCase;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeListener;
import org.oddjob.jobs.job.StopJob;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateOperator;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class StructuralJobTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(StructuralJobTest.class);
	
    @Before
    public void setUp() throws Exception {

		
		logger.info("------------------  " + getName() + "  -----------------");
	}
	
	private static class OurStructural extends StructuralJob<Runnable> {
		private static final long serialVersionUID = 1L;
		
		transient Runnable runnable;
		
		@Override
		protected StateOperator getInitialStateOp() {
			return new AnyActiveStateOp();
		}
		
		void setJob(Runnable c) {
			childHelper.insertChild(0, c);
		}
		
		protected void execute() {
			if (runnable != null) {
				runnable.run();
			}
		}
	}

   @Test
	public void testRunComplete() {
		final FlagState child = new FlagState(JobState.COMPLETE);
		
		final OurStructural test = new OurStructural();
		test.setJob(child);
		test.runnable = new Runnable() {
			public void run() {
				child.run();		
				assertEquals(ParentState.EXECUTING, test.lastStateEvent().getState());
			}
		};
		
		test.run();
		
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		assertEquals(JobState.READY, child.lastStateEvent().getState());
		assertEquals(ParentState.READY, test.lastStateEvent().getState());		
	}
	
   @Test
	public void testRunInComplete() {
		final FlagState child = new FlagState(JobState.INCOMPLETE);
		
		final OurStructural test = new OurStructural();
		test.setJob(child);
		test.runnable = new Runnable() {
			public void run() {
				child.run();		
				assertEquals(ParentState.EXECUTING, test.lastStateEvent().getState());
			}
		};
		
		test.run();
		
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
		
		child.setState(JobState.COMPLETE);
		child.softReset();
		child.run();
		
		assertEquals(JobState.COMPLETE, child.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());		
		
		test.hardReset();
		
		assertEquals(JobState.READY, child.lastStateEvent().getState());
		assertEquals(ParentState.READY, test.lastStateEvent().getState());		
	}

   @Test
	public void testRunStop() throws FailedToStopException, InterruptedException, ExecutionException, TimeoutException {
		final FlagState child = new FlagState(JobState.INCOMPLETE);
		
		final OurStructural test = new OurStructural();
		test.setJob(child);
				
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		
		final StopJob stop = new StopJob();
		stop.setJob(test);
		
		final AtomicReference<Future<?>> future = 
				new AtomicReference<Future<?>>();
		
		test.runnable = new Runnable() {
			public void run() {
				child.run();
				assertEquals(ParentState.EXECUTING, test.lastStateEvent().getState());
				future.set(executor.submit(stop));
			}
		};
		test.run();
		
		future.get().get(10, TimeUnit.SECONDS);
		
		assertEquals(JobState.COMPLETE, stop.lastStateEvent().getState());
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
		
		test.softReset();
		stop.hardReset();
		
		assertEquals(JobState.READY, child.lastStateEvent().getState());
		assertEquals(ParentState.READY, test.lastStateEvent().getState());		
		
		child.setState(JobState.COMPLETE);
		
		test.run();
		
		future.get().get(10, TimeUnit.SECONDS);
		
		assertEquals(JobState.COMPLETE, child.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		executor.shutdown();
	}
	
   @Test
	public void testJustChild() {
		final FlagState child = new FlagState(JobState.INCOMPLETE);
		child.run();
		
		final OurStructural test = new OurStructural();
		test.setJob(child);
		test.runnable = child;
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		test.softReset();
		
		assertEquals(JobState.READY, child.lastStateEvent().getState());
		assertEquals(ParentState.READY, test.lastStateEvent().getState());		
		
		child.setState(JobState.COMPLETE);
		test.run();
		
		assertEquals(JobState.COMPLETE, child.lastStateEvent().getState());
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());		
	}

   @Test
	public void testPersist() throws IOException, ClassNotFoundException {
		
		FlagState child = new FlagState(JobState.COMPLETE);
		OurStructural test = new OurStructural();
		
		test.setJob(child);
		test.run();
		child.run();
	
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		OurStructural copy = (OurStructural) OddjobTestHelper.copy(test);
		
		assertEquals(ParentState.COMPLETE, copy.lastStateEvent().getState());
	}
	
	class OurSession extends MockArooaSession {
		OurStructural saved;
		
		@Override
		public ComponentPool getComponentPool() {
			return new MockComponentPool() {
				@Override
				public void configure(Object component) {
				}
				@Override
				public void save(Object component) {
					if (component instanceof OurStructural) {
						try {
							saved = (OurStructural) OddjobTestHelper.copy(component);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					else {
						throw new RuntimeException("Unexpected.");
					}
				}
			};
		}
	}
	
   @Test
	public void testRunCompletePersist() {
		OurSession session = new OurSession();
		
		final FlagState child = new FlagState(JobState.COMPLETE);
		
		final OurStructural test = new OurStructural();
		test.setArooaSession(session);
		test.setJob(child);
		test.runnable = new Runnable() {
			public void run() {
				child.run();		
				assertEquals(ParentState.EXECUTING, test.lastStateEvent().getState());
			}
		};
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		
		OurStructural test2 = session.saved;
		
		final FlagState child2 = new FlagState(JobState.COMPLETE);
		
		test2.setArooaSession(session);
		test2.setJob(child2);
		test2.runnable = new Runnable() {
			public void run() {
				child2.run();		
				assertEquals(ParentState.EXECUTING, test.lastStateEvent().getState());
			}
		};
		
		assertEquals(ParentState.COMPLETE, test2.lastStateEvent().getState());
		assertEquals(JobState.READY, child2.lastStateEvent().getState());		
				
		test2.hardReset();
		
		assertEquals(JobState.READY, child2.lastStateEvent().getState());
		assertEquals(ParentState.READY, test2.lastStateEvent().getState());		
	}
	
	
	private class OurContext extends MockArooaContext {
		
		RuntimeListener listener;
		
		@Override
		public RuntimeConfiguration getRuntime() {
			return new MockRuntimeConfiguration() {
				@Override
				public void addRuntimeListener(RuntimeListener listener) {
					assertNull(OurContext.this.listener);
					assertNotNull(listener);
					OurContext.this.listener = listener;
				}
				
				@Override
				public void removeRuntimeListener(RuntimeListener listener) {
					super.removeRuntimeListener(listener);
				}
			};
		}
		
		@Override
		public ArooaSession getSession() {
			return new MockArooaSession();
		}
	}
	
	private class SimpleStructural extends StructuralJob<Runnable> {		
		private static final long serialVersionUID = 2010080500L;
		
		void setChild(Runnable child) {
			if (child == null) {
				childHelper.removeChildAt(0);
			}
			else {
				childHelper.insertChild(0, child);
			}
		}
		
		@Override
		protected void execute() throws Throwable {
			childHelper.getChild().run();
		}
		
		@Override
		protected StateOperator getInitialStateOp() {
			return new AnyActiveStateOp();
		}
	}
	
   @Test
	public void testChildDestroyed() throws FailedToStopException {
		
		SimpleJob component = new SimpleJob() {
			@Override
			protected int execute() throws Throwable {
				return 0;
			}
			
			@Override
			protected void save() throws ComponentPersistException {
			}
			@Override
			protected void configure() throws ArooaConfigurationException {
			}
		};
		
		OurContext childContext = new OurContext();
		component.setArooaContext(childContext);
				
		final SimpleStructural test = new SimpleStructural();
		
		StateSteps state = new StateSteps(test);
		state.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.COMPLETE, 
				ParentState.READY);
		
		test.setChild(component);
		
		test.run();
		
		childContext.listener.beforeDestroy(null);
		
		test.setChild(null);
		
		childContext.listener.afterDestroy(null);
		
		state.checkNow();
	}
	
	
	
   @Test
	public void testBothDestroyed() throws FailedToStopException {
		
		SimpleJob component = new SimpleJob() {
			@Override
			protected int execute() throws Throwable {
				return 0;
			}
			
			@Override
			protected void save() throws ComponentPersistException {
			}
			@Override
			protected void configure() throws ArooaConfigurationException {
			}
		};
		
		OurContext childContext = new OurContext();
		component.setArooaContext(childContext);
				
		final SimpleStructural test = new SimpleStructural() {
			private static final long serialVersionUID = 2010092900L;
			@Override
			protected void save() throws ComponentPersistException {
			}
			protected void configure() {
			}
		};
		
		OurContext structuralContext = new OurContext();
		test.setArooaContext(structuralContext);
		
		StateSteps state = new StateSteps(test);
		state.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.COMPLETE);
		
		test.setChild(component);
		
		test.run();
		
		state.checkNow();
		
		state.startCheck(ParentState.COMPLETE, 
				ParentState.DESTROYED);
		
		structuralContext.listener.beforeDestroy(null);
		childContext.listener.beforeDestroy(null);
		
		test.setChild(null);
		
		childContext.listener.afterDestroy(null);
		structuralContext.listener.afterDestroy(null);
		
		state.checkNow();
	}
	
	private class AnyStructural extends StructuralJob<Object> {
		private static final long serialVersionUID = 2010080500L;
		
		void setChild(Object child) {
			childHelper.insertChild(0, child);
		}
		
		@Override
		protected void execute() throws Throwable {
		}
		
		@Override
		protected StateOperator getInitialStateOp() {
			return new AnyActiveStateOp();
		}
	}
	
   @Test
	public void testChildStopped() throws FailedToStopException {

		final AtomicBoolean stopped = new AtomicBoolean();
		
		Object component = new Stoppable() {
			@Override
			public void stop() throws FailedToStopException {
				stopped.set(true);
			}
		};
		
				
		AnyStructural test = new AnyStructural();
		
		StateSteps check = new StateSteps(test);
		check.startCheck(ParentState.READY);
		test.setChild(component);
		
		test.stop();

		assertEquals(true, stopped.get());
		check.checkNow();
	}
}
