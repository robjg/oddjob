/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework.extend;

import org.oddjob.images.StateIcons;
import org.oddjob.state.StateDetail;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

abstract public class SerializableJob extends SimpleJob 
implements Serializable {
    private static final long serialVersionUID = 20050925;

    public SerializableJob() {
    	completeConstruction();
	}
    
	private void completeConstruction() {
	}
	
	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
		s.writeObject(getName());
		if (loggerName().startsWith(getClass().getName())) {
			s.writeObject(null);
		}
		else {
			s.writeObject(loggerName());
		}
		s.writeObject(stateHandler().lastStateEvent().serializable());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		String name = (String) s.readObject();
		logger((String) s.readObject());
		StateDetail savedEvent =
				(StateDetail) s.readObject();
		
		completeConstruction();
		
		setName(name);
		stateHandler().restoreLastJobStateEvent(savedEvent);
		iconHelper().changeIcon(
				StateIcons.iconFor(stateHandler().getState()));
	}

}
