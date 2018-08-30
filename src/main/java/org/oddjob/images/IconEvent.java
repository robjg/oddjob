package org.oddjob.images;

import java.io.Serializable;
import java.util.EventObject;

import org.oddjob.Iconic;

/**
 * An event which encapsulates information about a
 * variables value.
 * 
 * @author Rob Gordon
 */

public class IconEvent extends EventObject 
		implements Serializable {
	private static final long serialVersionUID = 2009061300L;
	
	final private String id;

	/**
	 * Event constructor.
	 * 
	 * @param source The source of the event.
	 * @param id The icon id.
	 */
	public IconEvent(Iconic source, String iconId) {

		super(source);
		this.id = iconId;
	}

	/**
	 * Get the variable name.
	 * 
	 * @return The variable name.
	 */

	public String getIconId() {
	
		return id;	
	}
	
	@Override
	public Iconic getSource() {
		return (Iconic) super.getSource();
	}
}
