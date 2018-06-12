package org.oddjob.framework.extend;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.ImageIcon;

import org.oddjob.Iconic;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.runtime.RuntimeEvent;
import org.oddjob.arooa.runtime.RuntimeListenerAdapter;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.framework.PropertyChangeNotifier;
import org.oddjob.framework.util.ComponentBoundry;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateHandler;
import org.oddjob.state.StateListener;
import org.oddjob.util.Restore;
import org.slf4j.Logger;

/**
 * An abstract implementation of a component which provides common 
 * functionality to jobs and services.
 * 
 * @author Rob Gordon
 */

public abstract class BaseComponent 
implements Iconic, Stateful, 
	ArooaSessionAware, ArooaContextAware, PropertyChangeNotifier {
	
    /**
	 * Implement property change support which sub classes can take advantage of.
	 */
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	/** 
	 * A state handler to delegate state change functionality to. 
	 * */
	private volatile ArooaSession session;

	/**
	 * Subclasses must provide a {@link StateHandler}.
	 * 
	 * @return A State Handler. Never null.
	 */
	abstract protected StateHandler<?> stateHandler();
	
	/**
	 * Subclasses must provide a {@link IconHelper}.
	 * 
	 * @return An Icon Helper. Never null.
	 */
	abstract protected IconHelper iconHelper();
	
	/**
	 * Here for the tests...
	 * 
	 * @param session
	 */
	@Override
	@ArooaHidden
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
		
	/**
	 * Allow sub classes access the the session.
	 * 
	 * @return A session. Will never be null once a component is initialised
	 * within Oddjob. May be null if otherwise if the session hasn't been
	 * set.
	 */
	protected ArooaSession getArooaSession() {
		return this.session;
	}
	
	@Override
	@ArooaHidden
	public void setArooaContext(ArooaContext context) {
		if (this instanceof ArooaLifeAware) {
			throw new IllegalStateException(getClass().getName() + 
					" must not implement " + ArooaLifeAware.class.getName() + 
					". Methods are already available and handled internally.");
		}
		
		context.getRuntime().addRuntimeListener(new RuntimeListenerAdapter() {
			@Override
			public void afterInit(RuntimeEvent event) throws ArooaException {
				onInitialised();
				stateHandler().waitToWhen(new IsAnyState(), 
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
				stateHandler().assertAlive();
				try (Restore restore = ComponentBoundry.push(logger().getName(), BaseComponent.this)) {
					logger().debug("Destroying.");
					onDestroy();
				}
			}
			
			@Override
			public void afterDestroy(RuntimeEvent event) throws ArooaException {
				
				try (Restore restore = ComponentBoundry.push(logger().getName(), BaseComponent.this)) {
					fireDestroyedState();
				}
			}
		});
		
	}
	
	protected abstract Logger logger();
		
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
	protected void configure(Object component) 
	throws ArooaConfigurationException {
		
		if (session != null) {
			logger().debug("Configuring.");
			session.getComponentPool().configure(component);
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
	@Override
	public StateEvent lastStateEvent() {
		return stateHandler().lastStateEvent();
	}
	
	/**
	 * Add a job state listener.
	 */
	@Override
	public void addStateListener(StateListener listener) {
		stateHandler().addStateListener(listener);
	}

	/**
	 * Remove a job state listener.
	 */
	@Override
	public void removeStateListener(StateListener listener){
		stateHandler().removeStateListener(listener);
	}
	
	/**
	 * Add a property change listener.
	 * 
	 * @param l The property change listener.
	 */		
	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		stateHandler().assertAlive();
		propertyChangeSupport.addPropertyChangeListener(l);		
	}

	/**
	 * Remove a property change listener.
	 * 
	 * @param l The property change listener.
	 */
	@Override
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
	@Override
	public ImageIcon iconForId(String iconId) {
		return iconHelper().iconForId(iconId);
	}

	/**
	 * Add an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	@Override
	public void addIconListener(IconListener listener) {
		stateHandler().assertAlive();
		iconHelper().addIconListener(listener);
	}

	/**
	 * Remove an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	@Override
	public void removeIconListener(IconListener listener) {
		iconHelper().removeIconListener(listener);
	}
	
	/**
	 * When running a job embedded in code, it may be necessary
	 * to call this method to initialise the job.
	 * 
	 * @throws JobDestroyedException 
	 */
	public void initialise() throws JobDestroyedException {
		stateHandler().assertAlive();
		try (Restore restore = ComponentBoundry.push(logger().getName(), this)) {
			onInitialised();
			onConfigured();
		}
	}	
	
	/**
	 * When running a job embedded in code, this method should always
	 * be called to clear up resources.
	 * 
	 * @throws JobDestroyedException 
	 */
	public void destroy() throws JobDestroyedException { 
		stateHandler().assertAlive();
		try (Restore restore = ComponentBoundry.push(logger().getName(), this)) {
			onDestroy();
			fireDestroyedState();
		}
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
	 * Subclasses override this method to clear up resources. This is
	 * called by the framework before child elements have been destroyed.
	 *
	 */
	protected void onDestroy() { }
	
	/**
	 * Subclasses must override this to fire the destroyed state.
	 * This is called by the framework after all child elements
	 * have been destroyed.
	 */
	abstract protected void fireDestroyedState();
	
}
