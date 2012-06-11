/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.control;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.monitor.model.DetailModel;
import org.oddjob.monitor.model.PropertyModel;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

/**
 *  Populate a property model with a subjects properties.
 *  
 *  @author Rob Gordon.
 */
public class PropertyPolling implements PropertyChangeListener {
	private static final Logger logger = Logger
			.getLogger(PropertyPolling.class);

	private Object subject;
	private Object kick;

	private PropertyModel propertyModel;

	private final UniversalDescriber describer;
	
	private final PropertyChangeListener subjectListener = 
		new PropertyChangeListener() {
		/**
		 * Change table model when a property changes.
		 * 
		 * @param e
		 *            The property change event.
		 */
		public void propertyChange(PropertyChangeEvent e) {
			synchronized (kick) {
				kick.notifyAll();
			}
		}
	};
	
	private final StateListener stateListener = 
		new StateListener() {
			
			@Override
			public void jobStateChange(StateEvent event) {
				synchronized (kick) {
					kick.notifyAll();
				}
			}
		};
	
	/**
	 * Constructor.
	 * 
	 * @param kick An object notify to kick the polling.
	 */
	public PropertyPolling(Object kick, ArooaSession session) {
		this.describer = new UniversalDescriber(session);
		this.kick = kick;
	}
	
	/**
	 * Poll for property changes.
	 *
	 */
	public void poll() {
		Object subject = getSubject();
		if (subject == null) {
			if (propertyModel != null) {
				propertyModel.setProperties(new HashMap<String, String>());
			}
		} else {
			Map<String, String> props = null;
			props = describer.describe(subject);
			propertyModel.setProperties(props);
		}
	}


	/**
	 * @return Returns the subject.
	 */
	public synchronized Object getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            The subject to set.
	 */
	public synchronized void setSubject(Object subject) {
		if (this.subject == subject) {
			return;
		}
		
		logger.debug("Setting subject to [" + subject + "]");
		
		if (this.subject != null) {
			PropertyChangeHelper.removePropertyChangeListener(
					this.subject,
					subjectListener);
			if (this.subject instanceof Stateful) {
				((Stateful) this.subject).removeStateListener(stateListener);
			}
		}
		this.subject = subject;
		if (this.subject != null) {
			PropertyChangeHelper.addPropertyChangeListener(
					this.subject, 
					subjectListener);
			if (this.subject instanceof Stateful) {
				((Stateful) this.subject).addStateListener(stateListener);
			}
		}
		synchronized (kick) {
			kick.notifyAll();
		}
	}

	/**
	 * @return Returns the propertyModel.
	 */
	public synchronized PropertyModel getPropertyModel() {
		return propertyModel;
	}

	/**
	 * @param propertyModel
	 *            The propertyModel to set.
	 */
	public synchronized void setPropertyModel(PropertyModel propertyModel) {
		this.propertyModel = propertyModel;
	}

	/**
	 * Called when the DetailModel changes. Usually when the job node
	 * select changes.
	 * 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		DetailModel explorerModel = (DetailModel) evt.getSource();
		if (explorerModel.getTabSelected() != DetailModel.PROPERTIES_TAB
				|| explorerModel.getSelectedJob() == null) {
			setSubject(null);
			return;
		}
		setSubject(explorerModel.getSelectedJob());
	}
	
}
