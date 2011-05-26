package org.oddjob.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Helper;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeListener;
import org.oddjob.images.StateIcons;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class BaseComponentTest extends TestCase {

	private static final Logger logger = Logger.getLogger(BaseComponentTest.class);
	
	private class OurComponent extends BaseComponent {

		@Override
		protected Logger logger() {
			return logger;
		}

		@Override
		protected void save() throws ComponentPersistException {
			OurComponent.this.save(OurComponent.this);
		}
		
		void complete() {
			stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
						getStateChanger().setJobState(JobState.COMPLETE);
				}
			});
		}
	}

	class OurSession extends MockArooaSession {
		@Override
		public ComponentPool getComponentPool() {
			return new MockComponentPool() {
				@Override
				public void save(Object component) throws ComponentPersistException {
					throw new ComponentPersistException("Deliberate fail.");
				}
			};
		}
	}
	
	public void testExceptionOnSave() {
		
		final OurComponent test = new OurComponent();
		
		test.setArooaSession(new OurSession());

		test.complete();
		
		assertEquals(JobState.EXCEPTION, test.lastJobStateEvent().getJobState());
		assertEquals(ComponentPersistException.class, 
				test.lastJobStateEvent().getException().getClass());
	}
	
	private static class SerializableComponent extends BaseComponent 
	implements Serializable {
		private static final long serialVersionUID = 2010042700L;

		public SerializableComponent() {
			
		}
		
		@Override
		protected Logger logger() {
			return logger;
		}		
		
		@Override
		protected void save() throws ComponentPersistException {
		}
		
		/**
		 * Custom serialisation.
		 */
		private void writeObject(ObjectOutputStream s) 
		throws IOException {
			s.defaultWriteObject();
			s.writeObject(stateHandler.lastJobStateEvent());
		}

		/**
		 * Custom serialisation.
		 */
		private void readObject(ObjectInputStream s) 
		throws IOException, ClassNotFoundException {
			s.defaultReadObject();
			assertNotNull(stateHandler);
			JobStateEvent savedEvent = (JobStateEvent) s.readObject();
			stateHandler.restoreLastJobStateEvent(savedEvent);
			iconHelper.changeIcon(
					StateIcons.iconFor(stateHandler.getJobState()));
		}
	}

	public void testSerialisation() throws IOException, ClassNotFoundException, InterruptedException {
		
		SerializableComponent test = new SerializableComponent();
		
		assertNotNull(test.stateHandler);
		JobStateEvent event = test.stateHandler.lastJobStateEvent();

		Thread.sleep(1L);
		
		SerializableComponent copy = Helper.copy(test);
		
		assertNotNull(copy.stateHandler);
		
		assertEquals(event.getTime(),
				copy.stateHandler.lastJobStateEvent().getTime());
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
					// TODO Auto-generated method stub
					super.removeRuntimeListener(listener);
				}
			};
		}
		
		@Override
		public ArooaSession getSession() {
			return new MockArooaSession();
		}
	}
	
	public void testStateNotifiedOnDestroy() {

		final List<JobState> results = new ArrayList<JobState>();

		final AtomicBoolean destroyed = new AtomicBoolean();
		BaseComponent test = new BaseComponent() {

			@Override
			protected Logger logger() {
				return logger;
			}
			
			@Override
			public void onDestroy() {
				super.onDestroy();
				destroyed.set(true);
			}
			
		};
				
		test.addJobStateListener(new JobStateListener() {
			
			@Override
			public void jobStateChange(JobStateEvent event) {
				results.add(event.getJobState());
			}
		});
		
		OurContext context = new OurContext();
		test.setArooaContext(context);
		
		context.listener.beforeDestroy(null);
		context.listener.afterDestroy(null);
		
		assertEquals(JobState.READY, results.get(0));
		assertEquals(JobState.DESTROYED, results.get(1));
		assertEquals(2, results.size());	
		
		assertTrue(destroyed.get());
	}
}
