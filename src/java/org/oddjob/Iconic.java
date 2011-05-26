/*
 * Copyright © 2004, Rob Gordon.
 */
package org.oddjob;

import org.oddjob.images.IconListener;
import org.oddjob.images.IconTip;


/**
 * An implementing class is able notify a visual display about
 * the icon and tool tip to associate with the object.
 * <p>
 * Icon notifications are strings, which the client may then
 * 'look up'. This allows the client to store an icon locally
 * thus reducing network traffic. This does limit the object to 
 * not changing the icon for a paticular id after startup.
 * 
 * @author Rob Gordon.
 */
public interface Iconic {

    /**
     * Return the IconTip (Icon and ToolTip pair).
     * 
     * @param id The icon id.
     * @return The IconTip or null if it doesn't exist.
     */
	public IconTip iconForId(String id);

	/**
	 * Add a listener.
	 * 
	 * @param listener The IconListener.
	 */
	public void addIconListener(IconListener listener);

	/**
	 * Remove a listener.
	 * 
	 * @param listener The IconListener.
	 */
	public void removeIconListener(IconListener listener);
}
