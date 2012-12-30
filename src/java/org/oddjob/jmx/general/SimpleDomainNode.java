package org.oddjob.jmx.general;

import java.util.concurrent.atomic.AtomicInteger;

import javax.management.ObjectName;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.oddjob.Iconic;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageIconStable;
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

	/** Used to count loggers. */
	private static final AtomicInteger instanceCount = new AtomicInteger();
	
	/** The icon. */
    private final static ImageIcon icon = new ImageIconStable(	
            IconHelper.class.getResource("Open16.gif"),
			"folder");

	/** Logger for this instance. */
	private final Logger logger = Logger.getLogger(getClass().getName() + 
			"." + instanceCount.incrementAndGet());
	
	/** The name. */
	private final String domain;
	
	/** Session. */
	private final MBeanSession mBeanSession;

	/** For structural. */
	private final ChildHelper<MBeanNode> childHelper = 
			new ChildHelper<MBeanNode>(this);

	/**
	 * Constructor.
	 * 
	 * @param domain The name.
	 * @param mBeanSession The session.
	 */
	public SimpleDomainNode(String domain, MBeanSession mBeanSession) {
		this.domain = domain;
		this.mBeanSession = mBeanSession;
	}
	
	@Override
	public String loggerName() {
		return logger.getName();
	}
	
	@Override
	public void initialise() {
		logger.info("Initialising for Domain: " + domain);
		
		MBeanCache cache = mBeanSession.getMBeanCache();
		try {
			MBeanNode[] children = cache.findBeans(
					new ObjectName(domain + ":*"));
			
			for (MBeanNode child : children) {
				childHelper.addChild(child);
				
				// done after add to allow logger archiver to be added.
				child.initialise();
			}
		} catch (Exception e) {
			logger.error("Failed Querying MBeanServer", e);
		} 
	}
	

	@Override
	public void addStructuralListener(StructuralListener listener) {
		childHelper.addStructuralListener(listener);
	}
	
	@Override
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}
	
	
	/**
	 * Return an icon tip for a given id. Part
	 * of the Iconic interface.
	 */
	public ImageIcon iconForId(String iconId) {
		return icon;
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
