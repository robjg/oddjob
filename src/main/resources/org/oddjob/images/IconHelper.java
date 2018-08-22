package org.oddjob.images;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;

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
	public static final String STARTABLE = "startable";
	public static final String EXECUTING = "executing";
	public static final String COMPLETE = "complete";
	public static final String NOT_COMPLETE = "notcomplete";
	public static final String EXCEPTION = "exception"; 
	public static final String SLEEPING = "sleeping";
	public static final String STOPPING = "stopping";
	public static final String STOPPED= "stopped";
	public static final String STARTED = "started"; 
	public static final String ACTIVE = "active"; 
	public static final String WAITING = "waiting"; 
	public static final String FIRING = "firing"; 
	public static final String TRIGGERED = "triggered"; 
	public static final String INVALID = "invalid"; 
	
	public static final ImageIcon nullIcon
		= new ImageIconStable(
				IconHelper.class.getResource("diamond.gif"),
				"Null Icon");
	
	public static final ImageIcon initializingIcon
		= new ImageIconStable(
				IconHelper.class.getResource("triangle.gif"),
				"Initialising");

	public static final ImageIcon readyIcon
		= new ImageIconStable(
				IconHelper.class.getResource("right_blue.gif"),
				"Ready");

	public static final ImageIcon startableIcon
		= new ImageIconStable(
				IconHelper.class.getResource("square_blue.gif"),
				"Startable");

	public static final ImageIcon executingIcon
		= new ImageIconStable(
				IconHelper.class.getResource("triangle_green.gif"),
				"Executing");

	public static final ImageIcon completeIcon
		= new ImageIconStable(
				IconHelper.class.getResource("tick_green.gif"),
				"Complete");

	public static final ImageIcon notCompleteIcon
		= new ImageIconStable(
				IconHelper.class.getResource("cross.gif"),
				"Not Complete");

	public static final ImageIcon stoppingIcon
		= new ImageIconStable(
				IconHelper.class.getResource("triangle_red.gif"),
				"Stopping");

	public static final ImageIcon stoppedIcon
		= new ImageIconStable(
				IconHelper.class.getResource("square_green.gif"),
				"Stopped");

	public static final ImageIcon sleepingIcon
		= new ImageIconStable(
				IconHelper.class.getResource("dot_blue.gif"),
				"Sleeping");

	public static final ImageIcon invalidIcon
		= new ImageIconStable(
				IconHelper.class.getResource("cross_red.gif"),
				"Invalid");

	public static final ImageIcon exceptionIcon
		= new ImageIconStable(
				IconHelper.class.getResource("asterix_red.gif"),
				"Exception");

	public static final ImageIcon startedIcon
		= new ImageIconStable(
				IconHelper.class.getResource("dot_green.gif"),
				"Started");

	public static final ImageIcon activeIcon
		= new ImageIconStable(
			IconHelper.class.getResource("right_green.gif"),
			"Active");
	
	public static final ImageIcon waitingIcon
	= new ImageIconStable(
		IconHelper.class.getResource("diamond_blue.gif"),
		"Waiting");

	public static final ImageIcon firingIcon
	= new ImageIconStable(
		IconHelper.class.getResource("star_red.gif"),
		"Firing");
	
	public static final ImageIcon triggeredIcon
	= new ImageIconStable(
		IconHelper.class.getResource("star_green.gif"),
		"Triggered");

	private static Map<String, ImageIcon> defaultIconMap = 
		new ConcurrentHashMap<String, ImageIcon>();

	static {
		defaultIconMap.put(NULL, nullIcon);
		defaultIconMap.put(INITIALIZING, initializingIcon);
		defaultIconMap.put(READY ,readyIcon);
		defaultIconMap.put(STARTABLE, startableIcon);
		defaultIconMap.put(EXECUTING, executingIcon);
		defaultIconMap.put(COMPLETE, completeIcon);
		defaultIconMap.put(NOT_COMPLETE, notCompleteIcon);
		defaultIconMap.put(SLEEPING, sleepingIcon);
		defaultIconMap.put(STOPPING, stoppingIcon);
		defaultIconMap.put(STOPPED, stoppedIcon);
		defaultIconMap.put(INVALID, invalidIcon);
		defaultIconMap.put(EXCEPTION, exceptionIcon);
		defaultIconMap.put(STARTED, startedIcon);
		defaultIconMap.put(WAITING, waitingIcon);
		defaultIconMap.put(FIRING, firingIcon);
		defaultIconMap.put(TRIGGERED, triggeredIcon);
		defaultIconMap.put(ACTIVE, activeIcon);
	}

	private final Iconic source;
	private volatile IconEvent lastEvent;
	private final List<IconListener> listeners = new ArrayList<IconListener>();
	private final Map<String, ImageIcon> iconMap;
	
	/**
	 * Constructor with default icon map.
	 * 
	 * @param source The source for events.
	 * @param intialId The initial icon id.
	 */
	public IconHelper(Iconic source, String initialId) {
		this(source, initialId, defaultIconMap);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param source The source for events.
	 * @param intialId The initial icon id.
	 * @param iconMap The map of ids to icons to use.
	 */
	public IconHelper(Iconic source, String initialId,
			Map<String, ImageIcon> iconMap) {
		this.source = source;
		lastEvent = new IconEvent(source, initialId);
		this.iconMap = iconMap;
	}
	
	/**
	 * Return an ImageIcon for the given id.
	 * 
	 * @param iconId The id.
	 * @return The ImageIcon never null.
	 * 
	 * @throws NoSuchIconException If the icon doesn't exist.
	 */
	@Override
	public ImageIcon iconForId(String iconId) {
		return iconMap.get(iconId);
	}

	/**
	 * Change the icon by firing an iconEvent.
	 * 
	 * @param iconId The icon id.
	 */
	public void changeIcon(String iconId) {
		
		// check icon
		if (!iconMap.containsKey(iconId)) {
			throw new IllegalArgumentException("No icon for " + iconId);
		}
		
		// create a local last event so that another thread
		// doesn't change the event mid notification. Copy the 
		// list of listeners so that we don't need to
		// hold the monitor lock when we notify them.
		IconEvent localEvent = new IconEvent(source, iconId);
		IconListener[] listenersCopy = null;
		
		synchronized (listeners) {
			if (iconId.equals(lastEvent.getIconId())) {
				return;
			}
			
			lastEvent = localEvent;
			listenersCopy = listeners.toArray(
					new IconListener[listeners.size()]);
		}
		for (int i = 0; i < listenersCopy.length; ++i) {
			listenersCopy[i].iconEvent(localEvent);
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
