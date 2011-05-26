package org.oddjob.framework;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.Iconic;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.runtime.RuntimeEvent;
import org.oddjob.arooa.runtime.RuntimeListenerAdaptor;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.IconTip;
import org.oddjob.images.StateIcons;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsSaveable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.JobStateListener;
import org.oddjob.state.StateChanger;

/**
 * An abstract implementation of a component which provides common functionality to
 * concrete sub classes.
 * 
 * @author Rob Gordon
 */

public abstract class BaseComponent 
implements Iconic, Stateful, 
	PropertyChangeNotifier,
	ArooaContextAware {
	
    /**
	 * Implement property change support which sub classes can take advantage of.
	 */
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * Used to notify clients of an icon change.
	 */
	protected final IconHelper iconHelper = new IconHelper(this);
	
	/** 
	 * A state handler to delegate state change functionality to. 
	 * */
	protected final JobStateHandler stateHandler = new JobStateHandler(this);
	
	private ArooaSession session;

	/**
	 * Here for the tests...
	 * 
	 * @param session
	 */
	@ArooaHidden
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@ArooaHidden
	public void setArooaContext(ArooaContext context) {
		if (this instanceof ArooaLifeAware) {
			throw new IllegalStateException(getClass().getName() + 
					" must not implement " + ArooaLifeAware.class.getName() + 
					". Methods are already available and handled internally.");
		}
		
		this.session = context.getSession();
		
		context.getRuntime().addRuntimeListener(new RuntimeListenerAdaptor() {
			@Override
			public void afterInit(RuntimeEvent event) throws ArooaException {
				onInitialised();
				stateHandler.waitToWhen(new IsAnyState(), 
						new Runnable() {
					@Override
					public void run() {
						// This is here purely for the safe publication
						// of state from this creating thread to the other
						// threads including the thread that will run the job.
					}
				});
			}

			@Override
			public void afterConfigure(RuntimeEvent event)
					throws ArooaException {
				onConfigured();
			}
			
			@Override
			public void beforeDestroy(RuntimeEvent event) throws ArooaException {
				stateHandler.assertAlive();
				logger().debug("[" + BaseComponent.this + "] destroying.");
				onDestroy();
			}
			
			@Override
			public void afterDestroy(RuntimeEvent event) throws ArooaException {
				
				fireDestroyedState();
			}
		});
		
	}
	
	protected ArooaSession getArooaSession() {
		return this.session;
	}
	
	protected abstract Logger logger();
	
	
	protected StateChanger getStateChanger() {
		return new StateChanger() {
			
			public void setJobState(JobState state) {
				setJobState(state, new Date());
			}
			
			public void setJobState(JobState state, Date date) {
				if (state == stateHandler.getJobState()) {
					return;
				}
				
				stateHandler.setJobState(state, date);
				iconHelper.changeIcon(StateIcons.iconFor(state));
				
				try {
					
					if (new IsSaveable().test(state)) {
							save();
					}
					
					stateHandler.fireEvent();
					
					
				} catch (ComponentPersistException e) {
					setJobStateException(e);
				}
			}
			
			public void setJobStateException(Throwable t) {
				setJobStateException(t, new Date());
			}
			
			public void setJobStateException(Throwable t, Date date) {
				if (JobState.EXCEPTION == stateHandler.getJobState()) {
					return;
				}
				
				stateHandler.setJobStateException(t, date);
				iconHelper.changeIcon(IconHelper.EXCEPTION);
				
				if (new IsSaveable().test(JobState.EXCEPTION) &&
						!(t instanceof ComponentPersistException)) {
					try {
						save();
					} catch (ComponentPersistException e) {
						stateHandler.setJobStateException(e, date);
					}
				}
				
				stateHandler.fireEvent();
				
			}
			
		};
	}
	
	/**
	 * Implementations override this to save their state on state
	 * change.
	 * 
	 * @throws ComponentPersistException
	 */
	protected void save() throws ComponentPersistException {
		
	}
	
	/**
	 * Configure the runtime. If there is no runtime for this
	 * component then true is returned.
	 * 
	 * @return true if successful. False if not.
	 */
	protected void configure(Object o) 
	throws ArooaConfigurationException {
		
		if (session != null) {
			logger().debug("Configuring job [" + o + "]");
			session.getComponentPool().configure(o);
		}
	}
	
	/**
	 * Save this job. If there is no runtime for this
	 * component then true is returned.
	 * 
	 * @return true if successful. False if not.
	 * @throws ComponentPersistException 
	 */
	protected void save(final Object o) throws ComponentPersistException {
		if (session != null) {
			session.getComponentPool().save(o);
		}
	}

	/**
	 * Returns the last JobState event. This is useful when Jobs are being
	 * used directly in code, and only one thread is using the job. Otherwise 
	 * a JobStateListener should always be used.
	 * <p>
	 * This is not a property so that it can't be accessed directly in scripts.
	 * 
	 * @return The last JobStateEvent. Will never be null.
	 */	
	public JobStateEvent lastJobStateEvent() {
		return stateHandler.lastJobStateEvent();
	}
	
	/**
	 * Add a job state listener.
	 */
	public void addJobStateListener(JobStateListener listener) {
		stateHandler.addJobStateListener(listener);
	}

	/**
	 * Remove a job state listener.
	 */
	public void removeJobStateListener(JobStateListener listener){
		stateHandler.removeJobStateListener(listener);
	}
	
	/**
	 * Add a property change listener.
	 * 
	 * @param l The property change listener.
	 */		
	public void addPropertyChangeListener(PropertyChangeListener l) {
		stateHandler.assertAlive();
		propertyChangeSupport.addPropertyChangeListener(l);		
	}

	/**
	 * Remove a property change listener.
	 * 
	 * @param l The property change listener.
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}

	/**
	 * Fire a property change event.
	 * 
	 * @param propertyName
	 * @param oldValue
	 * @param newValue
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	/**
	 * Return an icon tip for a given id. Part
	 * of the Iconic interface.
	 */
	public IconTip iconForId(String iconId) {
		return iconHelper.iconForId(iconId);
	}

	/**
	 * Add an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	public void addIconListener(IconListener listener) {
		stateHandler.assertAlive();
		iconHelper.addIconListener(listener);
	}

	/**
	 * Remove an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	public void removeIconListener(IconListener listener) {
		iconHelper.removeIconListener(listener);
	}
	
	/**
	 * When running a job embedded in code, it may be necessary
	 * to call this method to initialise the job.
	 */
	public void initialise() {
		stateHandler.assertAlive();
		onInitialised();
		onConfigured();
	}	
	
	/**
	 * When running a job embedded in code, this method should always
	 * be called to clear up resources.
	 */
	public void destroy() { 
		stateHandler.assertAlive();
		onDestroy();
		fireDestroyedState();
	}
	
	/**
	 * Subclasses override this method to perform post creation
	 * initialisation.
	 *
	 */
	protected void onInitialised() {
	}
	
	/**
	 * Subclasses override this method to perform post configuration
	 * initialisation.
	 *
	 */
	protected void onConfigured() {
	}
	
	
	/**
	 * Subclasses override this method to clear up resources.
	 *
	 */
	protected void onDestroy() { }
	
	/**
	 * Internal method to fire state.
	 */
	private void fireDestroyedState() {
		
		if (!stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler.setJobState(JobState.DESTROYED);
				stateHandler.fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + BaseComponent.this + " Failed set state DESTROYED");
		}
		logger().debug("[" + BaseComponent.this + "] destroyed.");				
	}
}
