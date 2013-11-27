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
import org.oddjob.OddjobTestHelper;
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
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateChanger;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateHandler;

public class BaseComponentTest extends TestCase {

	private static final Logger logger = Logger.getLogger(BaseComponentTest.class);
	
	private class OurComponent extends BaseComponent {

		private final JobStateHandler stateHandler = new JobStateHandler(this);
		
		private final JobStateChanger stateChanger;
		
		protected OurComponent() {
			stateChanger = new JobStateChanger(stateHandler, iconHelper, 
					new Persistable() {					
						@Override
						public void persist() throws ComponentPersistException {
							save();
						}
					});
		}
		
		@Override
		protected StateHandler<?> stateHandler() {
			return stateHandler;
		}
		
		protected StateChanger<JobState> getStateChanger() {
			return stateChanger;
		}
		
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
						getStateChanger().setState(JobState.COMPLETE);
				}
			});
		}
		
		@Override
		protected void fireDestroyedState() {
			throw new RuntimeException("Unexpected");
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
		
		assertEquals(JobState.EXCEPTION, test.lastStateEvent().getState());
		assertEquals(ComponentPersistException.class, 
				test.lastStateEvent().getException().getClass());
	}
	
	private static class SerializableComponent extends BaseComponent 
	implements Serializable {
		private static final long serialVersionUID = 2010042700L;

		transient JobStateHandler stateHandler;
		
		public SerializableComponent() {
			completeConstruction();
		}

		private void completeConstruction() {
			stateHandler = new JobStateHandler(this);
		}
		
		@Override
		protected StateHandler<?> stateHandler() {
			return stateHandler;
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
			s.writeObject(stateHandler.lastStateEvent());
		}

		/**
		 * Custom serialisation.
		 */
		private void readObject(ObjectInputStream s) 
		throws IOException, ClassNotFoundException {
			s.defaultReadObject();
			assertNotNull(iconHelper);
			StateEvent savedEvent = (StateEvent) s.readObject();
			
			completeConstruction();
			
			stateHandler.restoreLastJobStateEvent(savedEvent);
			iconHelper.changeIcon(
					StateIcons.iconFor(stateHandler.getState()));
		}
		
		@Override
		protected void fireDestroyedState() {
			throw new RuntimeException("Unexpected.");
		}
	}

	public void testSerialisation() throws IOException, ClassNotFoundException, InterruptedException {
		
		SerializableComponent test = new SerializableComponent();
		
		assertNotNull(test.stateHandler);
		StateEvent event = test.stateHandler.lastStateEvent();

		Thread.sleep(1L);
		
		SerializableComponent copy = OddjobTestHelper.copy(test);
		
		assertNotNull(copy.stateHandler);
		
		assertEquals(event.getTime(),
				copy.stateHandler.lastStateEvent().getTime());
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
	
	/** Note this test should change classes as it test the BasePrimary. */
	public void testStateNotifiedOnDestroy() {

		final List<State> results = new ArrayList<State>();

		final AtomicBoolean destroyed = new AtomicBoolean();
		
		BasePrimary test = new BasePrimary() {

			JobStateHandler stateHandler = new JobStateHandler(this);
			
			@Override
			protected StateHandler<?> stateHandler() {
				return stateHandler;
			}
			
			@Override
			protected Logger logger() {
				return logger;
			}
			
			@Override
			public void onDestroy() {
				super.onDestroy();
				destroyed.set(true);
			}
			
			@Override
			protected void fireDestroyedState() {
				
				if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						stateHandler.setState(JobState.DESTROYED);
						stateHandler.fireEvent();
					}
				})) {
					throw new IllegalStateException("Failed set state DESTROYED");
				}
			}
		};
				
		test.addStateListener(new StateListener() {
			
			@Override
			public void jobStateChange(StateEvent event) {
				results.add(event.getState());
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
