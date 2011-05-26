/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.StateSteps;
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
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;

/**
 * 
 */
public class StructuralJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(StructuralJobTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("------------------  " + getName() + "  -----------------");
	}
	
	private static class OurStructural extends StructuralJob<Runnable> {
		private static final long serialVersionUID = 1L;
		
		transient Runnable runnable;
		
		@Override
		protected StateOperator getStateOp() {
			return new WorstStateOp();
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

	public void testRunComplete() {
		final FlagState child = new FlagState(JobState.COMPLETE);
		
		final OurStructural test = new OurStructural();
		test.setJob(child);
		test.runnable = new Runnable() {
			public void run() {
				child.run();		
				assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());
			}
		};
		test.onInitialised();
		
		test.run();
		
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		
		assertEquals(JobState.READY, child.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());		
	}
	
	public void testRunInComplete() {
		final FlagState child = new FlagState(JobState.INCOMPLETE);
		
		final OurStructural test = new OurStructural();
		test.setJob(child);
		test.runnable = new Runnable() {
			public void run() {
				child.run();		
				assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());
			}
		};
		test.onInitialised();
		
		test.run();
		
		
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
		
		child.setState(JobState.COMPLETE);
		child.softReset();
		child.run();
		
		assertEquals(JobState.COMPLETE, child.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());		
		
		test.hardReset();
		
		assertEquals(JobState.READY, child.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());		
	}

	public void testRunStop() throws FailedToStopException {
		final FlagState child = new FlagState(JobState.INCOMPLETE);
		
		final OurStructural test = new OurStructural();
		test.setJob(child);
				
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		final StopJob stop = new StopJob();
		stop.setAsync(true);
		stop.setJob(test);
		stop.setExecutorService(executor);
		
		test.runnable = new Runnable() {
			public void run() {
				child.run();
				assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());
				stop.run();
			}
		};
		test.run();
		
		executor.shutdown();
		
		assertEquals(JobState.COMPLETE, stop.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
		
		test.softReset();
		
		assertEquals(JobState.READY, child.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());		
		
		child.setState(JobState.COMPLETE);
		test.run();
		
		assertEquals(JobState.COMPLETE, child.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());		
	}
	
	public void testJustChild() {
		final FlagState child = new FlagState(JobState.INCOMPLETE);
		child.run();
		
		final OurStructural test = new OurStructural();
		test.setJob(child);
		test.runnable = child;
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		test.softReset();
		
		assertEquals(JobState.READY, child.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());		
		
		child.setState(JobState.COMPLETE);
		test.run();
		
		assertEquals(JobState.COMPLETE, child.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());		
	}

	public void testPersist() throws IOException, ClassNotFoundException {
		
		FlagState child = new FlagState(JobState.COMPLETE);
		OurStructural test = new OurStructural();
		
		test.setJob(child);
		test.run();
		child.run();
	
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		OurStructural copy = (OurStructural) Helper.copy(test);
		
		assertEquals(JobState.COMPLETE, copy.lastJobStateEvent().getJobState());
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
							saved = (OurStructural) Helper.copy(component);
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
	
	public void testRunCompletePersist() {
		OurSession session = new OurSession();
		
		final FlagState child = new FlagState(JobState.COMPLETE);
		
		final OurStructural test = new OurStructural();
		test.setArooaSession(session);
		test.setJob(child);
		test.runnable = new Runnable() {
			public void run() {
				child.run();		
				assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());
			}
		};
		test.onInitialised();
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		
		OurStructural test2 = session.saved;
		
		final FlagState child2 = new FlagState(JobState.COMPLETE);
		
		test2.setArooaSession(session);
		test2.setJob(child2);
		test2.runnable = new Runnable() {
			public void run() {
				child2.run();		
				assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());
			}
		};
		test2.onInitialised();
		
		assertEquals(JobState.COMPLETE, test2.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, child2.lastJobStateEvent().getJobState());		
				
		test2.hardReset();
		
		assertEquals(JobState.READY, child2.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test2.lastJobStateEvent().getJobState());		
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
		protected StateOperator getStateOp() {
			return new WorstStateOp();
		}
	}
	
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
		state.startCheck(JobState.READY, 
				JobState.EXECUTING, JobState.COMPLETE, JobState.READY);
		
		test.setChild(component);
		
		test.run();
		
		childContext.listener.beforeDestroy(null);
		
		test.setChild(null);
		
		childContext.listener.afterDestroy(null);
		
		state.checkNow();
	}
	
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
		state.startCheck(JobState.READY, 
				JobState.EXECUTING, JobState.COMPLETE, JobState.DESTROYED);
		
		test.setChild(component);
		
		test.run();
		
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
		protected StateOperator getStateOp() {
			return new WorstStateOp();
		}
	}
	
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
		check.startCheck(JobState.READY);
		test.setChild(component);
		
		test.stop();

		assertEquals(true, stopped.get());
		check.checkNow();
	}
}
