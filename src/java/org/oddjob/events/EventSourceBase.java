package org.oddjob.events;

import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.events.state.EventState;
import org.oddjob.events.state.EventStateChanger;
import org.oddjob.events.state.EventStateHandler;
import org.oddjob.framework.extend.BasePrimary;
import org.oddjob.framework.util.ComponentBoundry;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.State;
import org.oddjob.state.StateChanger;
import org.oddjob.util.Restore;

abstract public class EventSourceBase<T> extends BasePrimary
implements EventSource<T> {

	/** Handle state. */
	private transient volatile EventStateHandler stateHandler;
	
	/** Used to notify clients of an icon change. */
	private transient volatile IconHelper iconHelper;
	
	private transient volatile EventStateChanger stateChanger;

    /**
     * Constructor.
     */
    public EventSourceBase() {
    	completeConstruction();
	}

    private void completeConstruction() {
		stateHandler = new EventStateHandler(this);		
		iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		stateChanger = new EventStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
					@Override
					public void persist() throws ComponentPersistException {
						save();
					}
				});
	}

	@Override
	protected EventStateHandler stateHandler() {
		return stateHandler;
	}

	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	protected final StateChanger<EventState> getStateChanger() {
		return stateChanger;
	}
	
	@Override
	public final Restore start(Consumer<? super T> consumer) throws Exception {
		Objects.requireNonNull(consumer);
				
		if (!stateHandler().waitToWhen(new IsExecutable(), 
				() -> getStateChanger().setState(EventState.CONNECTING))) {
			throw new IllegalStateException("Not Stopped!");
		}

		final Semaphore barrier = new Semaphore(1);
		Consumer<T> consumerWrapper = value ->  {
			try (Restore boundary2 = ComponentBoundry.push(loggerName(), EventSourceBase.this)) {
				barrier.acquire();
				stateHandler().waitToWhen(s -> true, 
						() -> getStateChanger().setState(EventState.FIRING));
				logger().debug("Received event {}", value);
				consumer.accept(value);
				stateHandler().waitToWhen(new IsStoppable(), 
						() -> getStateChanger().setState(EventState.TRIGGERED));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			finally {
				barrier.release();
			}
		};

		try (Restore boundary = ComponentBoundry.push(loggerName(), this)) {

			logger().info("Starting");
			
			try {
				configure();
				
				
				Restore restore = doStart(consumerWrapper);
				
				stateHandler().waitToWhen(s -> s == EventState.CONNECTING, 
						() -> getStateChanger().setState(EventState.WAITING));
				
				return () -> {
					try (Restore boundary2 = ComponentBoundry.push(loggerName(), EventSourceBase.this)) {
						restore.close();
						logger().info("Stopped");
					}
					catch (RuntimeException e) {
						stateHandler().waitToWhen(s -> true, 
								() -> getStateChanger().setStateException(e));			
						throw e;
					}
					stateHandler().waitToWhen(new IsStoppable(), 
							() -> {
								State state = stateHandler().lastStateEvent().getState();
								if (state == EventState.TRIGGERED || state == EventState.FIRING) {
									getStateChanger().setState(EventState.COMPLETE);
								}
								else {
									getStateChanger().setState(EventState.INCOMPLETE);
								}
							});
				};	
				
		}
		catch (Exception e) {
			stateHandler().waitToWhen(new IsAnyState(), 
					() -> getStateChanger().setStateException(e));
			throw e;
		}
		}
	}
	
	protected void setStateException(Throwable e) {
		stateHandler().waitToWhen(s -> true, 
				() -> getStateChanger().setStateException(e));					
	}
	
	protected abstract Restore doStart(Consumer<? super T> consumer) throws Exception;
	
	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(EventState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + this + "] Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
