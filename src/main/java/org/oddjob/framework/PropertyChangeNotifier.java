package org.oddjob.framework;

import java.beans.PropertyChangeListener;

/**
 * A Bean that is able to notify listeners of property changes.
 * It would be nice if this was part of the standard Java Bean stuff, 
 * but it isn't.
 * 
 * @author rob
 *
 */
public interface PropertyChangeNotifier {

	/**
	 * Add a Listener. No validation is done for 
	 * if the property name is not 
	 * one for which the bean provides notifications.
	 * 
	 * @param listener The listener.
	 * 
	 */
   public void addPropertyChangeListener(
        PropertyChangeListener listener);

   /**
    * Remove a Listener.
    * 
    * @param listener The listener.
    * 
    */
   public void removePropertyChangeListener(
                PropertyChangeListener listener);

}