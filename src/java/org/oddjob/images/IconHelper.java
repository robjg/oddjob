package org.oddjob.images;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oddjob.Iconic;


/**
 * Icons for the JobTree. All from <a href="http://www.cit.gu.edu.au/images/Images.html">Anthony's WWW Images</a>. 
 * <a href="http://java.sun.com/developer/techDocs/hi/repository/">Java Look and Feel Graphics</a>.
 * 
 * @author Rob Gordon 
 */

public class IconHelper implements Iconic {

    public static final String NULL = "null";
	public static final String INITIALIZING = "initializing";
	public static final String READY = "ready";
	public static final String EXECUTING = "executing";
	public static final String COMPLETE = "complete";
	public static final String NOT_COMPLETE = "notcomplete";
	public static final String EXCEPTION = "exception"; 
	public static final String SLEEPING = "sleeping";
	public static final String STOPPING = "stopping";
	public static final String STOPPED= "stopped";
	public static final String INVALID = "invalid"; 
	
	public static final IconTip nullIcon
		= new IconTip(
				IconHelper.class.getResource("diamond.gif"),
				"Null Icon");
	
	public static final IconTip initializingIcon
		= new IconTip(
				IconHelper.class.getResource("triangle.gif"),
				"Initialising");

	public static final IconTip readyIcon
		= new IconTip(
				IconHelper.class.getResource("right_blue.gif"),
				"Ready");

	public static final IconTip executingIcon
		= new IconTip(
				IconHelper.class.getResource("triangle_green.gif"),
				"Executing");

	public static final IconTip completeIcon
		= new IconTip(
				IconHelper.class.getResource("tick_green.gif"),
				"Complete");

	public static final IconTip notCompleteIcon
		= new IconTip(
				IconHelper.class.getResource("cross.gif"),
				"Not Complete");

	public static final IconTip stoppingIcon
		= new IconTip(
				IconHelper.class.getResource("triangle_red.gif"),
				"Stopping");

	public static final IconTip stoppedIcon
	= new IconTip(
			IconHelper.class.getResource("square_red.gif"),
			"Stopped");

	public static final IconTip sleepingIcon
		= new IconTip(
				IconHelper.class.getResource("dot_blue.gif"),
				"Sleeping");

	public static final IconTip invalidIcon
		= new IconTip(
				IconHelper.class.getResource("cross_red.gif"),
				"Invalid");

	public static final IconTip exceptionIcon
		= new IconTip(
				IconHelper.class.getResource("asterix_red.gif"),
				"Exception");

	private static Map<String, IconTip> defaultIconMap = 
		new HashMap<String, IconTip>();

	static {
		defaultIconMap.put(NULL, nullIcon);
		defaultIconMap.put(INITIALIZING, initializingIcon);
		defaultIconMap.put(READY ,readyIcon);
		defaultIconMap.put(EXECUTING, executingIcon);
		defaultIconMap.put(COMPLETE, completeIcon);
		defaultIconMap.put(NOT_COMPLETE, notCompleteIcon);
		defaultIconMap.put(SLEEPING, sleepingIcon);
		defaultIconMap.put(STOPPING, stoppingIcon);
		defaultIconMap.put(STOPPED, stoppedIcon);
		defaultIconMap.put(INVALID, invalidIcon);
		defaultIconMap.put(EXCEPTION, exceptionIcon);
	}

	private final Iconic source;
	private volatile IconEvent lastEvent;
	private List<IconListener> listeners = new ArrayList<IconListener>();
	private final Map<String, IconTip> iconMap;
	
	public IconHelper(Iconic source) {
		this(source, defaultIconMap);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param source The source for events.
	 */
	public IconHelper(Iconic source, Map<String, IconTip> iconMap) {
		this.source = source;
		lastEvent = new IconEvent(source, READY);
		this.iconMap = iconMap;
	}
	
	/**
	 * Return an IconTip for the given id.
	 * 
	 * @param iconId The id.
	 * @return The iconTip never null.
	 * 
	 * @throws NoSuchIconException If the icon doesn't exist.
	 */
	@Override
	public IconTip iconForId(String iconId) {
		return iconMap.get(iconId);
	}

	/**
	 * Change the icon by firing an iconEvent.
	 * 
	 * @param iconId The icon id.
	 */
	public void changeIcon(String iconId) {
		if (iconId.equals(lastEvent.getIconId())) {
			return;
		}
		
		// check icon
		if (!iconMap.containsKey(iconId)) {
			throw new IllegalArgumentException("No icon for " + iconId);
		}
		
		// create a local last event so that another thread
		// doesn't change the event mid notification. Copy the 
		// list of lisetners so that we don't need to
		// hold the monitor lock when we notify them.
		IconEvent localEvent = new IconEvent(source, iconId);
		IconListener[] la = null;
		synchronized (listeners) {
			lastEvent = localEvent;
			la = (IconListener[]) listeners.toArray(new IconListener[0]);
		}
		for (int i = 0; i < la.length; ++i) {
			la[i].iconEvent(localEvent);
		}
	}

	/**
	 * Get the current/last icon id.
	 * 
	 * @return An iconId.
	 */
	public String currentId() {
		return lastEvent.getIconId();
	}
	
	public void addIconListener(IconListener listener) {
		if (lastEvent == null) {
			throw new IllegalStateException("No icon set in " + source);
		}
		listener.iconEvent(lastEvent);
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public void removeIconListener(IconListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}		
}
