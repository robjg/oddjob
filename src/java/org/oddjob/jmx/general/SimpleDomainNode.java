package org.oddjob.jmx.general;

import java.util.concurrent.atomic.AtomicInteger;

import javax.management.ObjectName;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.oddjob.Iconic;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.logging.LogEnabled;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * A simple implementation of a {@link DomainNode}.
 * 
 * @author rob
 *
 */
public class SimpleDomainNode implements DomainNode, Iconic, LogEnabled {

	private static final AtomicInteger instanceCount = new AtomicInteger();
	
	private final String loggerName = getClass().getName() + 
			"." + instanceCount.incrementAndGet();
	
	private final Logger logger = Logger.getLogger(loggerName);
	
	private final String domain;
	
	private final MBeanSession mBeanSession;

	private final ChildHelper<MBeanNode> childHelper = 
			new ChildHelper<MBeanNode>(this);
	
	public SimpleDomainNode(String domain, MBeanSession mBeanSession) {
		this.domain = domain;
		this.mBeanSession = mBeanSession;
	}
	
	@Override
	public String loggerName() {
		return loggerName;
	}

	@Override
	public void addStructuralListener(StructuralListener listener) {
		synchronized (childHelper) {
			if (childHelper.isNoListeners()) {
				MBeanCache cache = mBeanSession.getMBeanCache();
				try {
					MBeanNode[] children = cache.findBeans(
							new ObjectName(domain + ":*"));
					
					for (MBeanNode child : children) {
						childHelper.addChild(child);
					}
				} catch (Exception e) {
					logger.error("Failed Querying MBeanServer", e);
				} 
			}
			childHelper.addStructuralListener(listener);
		}
	}
	
	@Override
	public void removeStructuralListener(StructuralListener listener) {
		synchronized (childHelper) {
			childHelper.removeStructuralListener(listener);
			if (childHelper.isNoListeners()) {
				childHelper.removeAllChildren();
			}
		}
	}
	
	
	/**
	 * Return an icon tip for a given id. Part
	 * of the Iconic interface.
	 */
	public ImageIcon iconForId(String iconId) {
		if (iconId.equals("folder")) {
		    return new ImageIcon(	
		            IconHelper.class.getResource("Open16.gif"),
					"folder");
		} 
		else {
		    return null;
		}
	}
	
	/**
	 * Add an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	public void addIconListener(IconListener listener) {
		listener.iconEvent(new IconEvent(this, "folder"));
	}

	/**
	 * Remove an icon listener. Part of the Iconic
	 * interface.
	 * 
	 * @param listener The listener.
	 */
	public void removeIconListener(IconListener listener) {
	}

	
	public void destroy() {
		while (childHelper.size() > 0) {
			MBeanNode node = childHelper.removeChildAt(0);
			node.destroy();
		}
	}
	
	@Override
	public String toString() {
		return domain;
	}
}
