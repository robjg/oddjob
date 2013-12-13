/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.oddjob.Structural;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * Utility class to wait for a certain number of children to be
 * added to a structural node.
 *
 */
public class WaitForChildren implements StructuralListener {
	private static final Logger logger = Logger.getLogger(WaitForChildren.class);
	
	private final Structural structural; 
	private List<Object> children;
	
	private final int retry = 3;
	
	public WaitForChildren(Object o) {
		structural = (Structural) o;
	}
	
	public void waitFor(int count) {
		children = new ArrayList<Object>();
		structural.addStructuralListener(this);
		try {
			synchronized (this) {
				for (int i = 0; i < retry && children.size() != count; ++i) {
					logger.debug("Waiting for [" + structural +
							"] to have [" + count + "] children (has " 
							+ children.size() + ")");
					wait(5000);
				}
				if (children.size() != count) {
					throw new RuntimeException("Giving up waiting for [ + " +
						structural + "] to have [" + count + 
						"] children. Children so far: " + 
						Arrays.toString(children.toArray(
								new Object[children.size()])));
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			structural.removeStructuralListener(this);
		}
		
	}	
	
	synchronized public Object[] children() {
		return children.toArray();
	}
	
	synchronized public void childAdded(StructuralEvent event) {
		logger.debug("Child [" + event.getIndex() + "] adding ["
				+ event.getChild() + "]"); 
		try {
			children.add(event.getIndex(), event.getChild());
		}
		catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Failed adding child to [" + 
					structural + "] " + e.getMessage());
		}
		notifyAll();
	}
	
	synchronized public void childRemoved(StructuralEvent event) {
		logger.debug("Child [" + event.getIndex() + "] removed ["
				+ event.getChild() + "]"); 
		try {
			children.remove(event.getIndex());
		}
		catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Failed removing child from [" + 
					structural + "] " + e.getMessage());
		}
		notifyAll();
	}
}