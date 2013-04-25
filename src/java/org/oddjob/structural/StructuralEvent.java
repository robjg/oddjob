package org.oddjob.structural;

import java.io.Serializable;
import java.util.EventObject;

import org.oddjob.Structural;

/**
 * This event is fired by an implementer of the {@link Structural} interface 
 * when its structure changes.
 * 
 * @see StructuralListener
 * 
 * @author Rob Gordon
 */
public class StructuralEvent extends EventObject 
		implements Serializable {
	
	private static final long serialVersionUID = 2009042400L;
	
	/** The child that has been added or just removed */
	private final Object child;
	
	/** The position the child was added or removed (starting at 0). */
	private final int index;
	
	/**
	 * Constructor.
	 * 
	 * @param source The source of the event. Generally the parent.
	 * @param child The child object that has been added or removed.
	 * @param index The position where it was added or removed (starting at 0).
	 */
	public StructuralEvent(Structural source, Object child, int index) {
		
		super(source);
		this.child = child;
		this.index = index;
	}
	
	/**
	 * Get the child.
	 * 
	 * @return The child.
	 */
	public Object getChild() {
		
		return this.child;
	}
	
	/**
	 * Get the index.
	 * 
	 * @return The index.
	 */
	public int getIndex() {
		
		return this.index;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[child=" + child+ "]index[=" +
		index + "]";
	}
}
