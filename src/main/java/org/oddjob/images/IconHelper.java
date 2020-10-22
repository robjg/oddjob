package org.oddjob.images;

import org.oddjob.Iconic;
import org.oddjob.util.OddjobWrapperException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


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
	
	public static final ImageData nullIcon;
	
	public static final ImageData initializingIcon;

	public static final ImageData readyIcon;

	public static final ImageData startableIcon;

	public static final ImageData executingIcon;

	public static final ImageData completeIcon;

	public static final ImageData notCompleteIcon;

	public static final ImageData stoppingIcon;

	public static final ImageData stoppedIcon;

	public static final ImageData sleepingIcon;

	public static final ImageData invalidIcon;

	public static final ImageData exceptionIcon;

	public static final ImageData startedIcon;

	public static final ImageData activeIcon;
	
	public static final ImageData waitingIcon;

	public static final ImageData firingIcon;
	
	public static final ImageData triggeredIcon;

	static {

		try {
			nullIcon = ImageData.fromUrl(
					IconHelper.class.getResource("diamond.gif"),
					"Null Icon");

			initializingIcon = ImageData.fromUrl(
					IconHelper.class.getResource("triangle.gif"),
					"Initialising");

			readyIcon = ImageData.fromUrl(
					IconHelper.class.getResource("right_blue.gif"),
					"Ready");

			startableIcon = ImageData.fromUrl(
					IconHelper.class.getResource("square_blue.gif"),
					"Startable");

			executingIcon = ImageData.fromUrl(
					IconHelper.class.getResource("triangle_green.gif"),
					"Executing");

			completeIcon = ImageData.fromUrl(
					IconHelper.class.getResource("tick_green.gif"),
					"Complete");

			notCompleteIcon = ImageData.fromUrl(
					IconHelper.class.getResource("cross.gif"),
					"Not Complete");

			stoppingIcon = ImageData.fromUrl(
					IconHelper.class.getResource("triangle_red.gif"),
					"Stopping");

			stoppedIcon = ImageData.fromUrl(
					IconHelper.class.getResource("square_green.gif"),
					"Stopped");

			sleepingIcon = ImageData.fromUrl(
					IconHelper.class.getResource("dot_blue.gif"),
					"Sleeping");

			invalidIcon = ImageData.fromUrl(
					IconHelper.class.getResource("cross_red.gif"),
					"Invalid");

			exceptionIcon = ImageData.fromUrl(
					IconHelper.class.getResource("asterix_red.gif"),
					"Exception");

			startedIcon = ImageData.fromUrl(
					IconHelper.class.getResource("dot_green.gif"),
					"Started");

			activeIcon = ImageData.fromUrl(
					IconHelper.class.getResource("right_green.gif"),
					"Active");

			waitingIcon = ImageData.fromUrl(
					IconHelper.class.getResource("diamond_blue.gif"),
					"Waiting");

			firingIcon = ImageData.fromUrl(
					IconHelper.class.getResource("star_red.gif"),
					"Firing");

			triggeredIcon = ImageData.fromUrl(
					IconHelper.class.getResource("star_green.gif"),
					"Triggered");
		}
		catch (IOException e) {
			throw new IOError(e);
		}
	}

	private static final Map<String, ImageData> defaultIconMap =
			new ConcurrentHashMap<>();

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
	private final List<IconListener> listeners = new ArrayList<>();
	private final Map<String, ImageData> iconMap;
	
	/**
	 * Constructor with default icon map.
	 * 
	 * @param source The source for events.
	 * @param initialId The initial icon id.
	 */
	public IconHelper(Iconic source, String initialId) {
		this(source, initialId, defaultIconMap);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param source The source for events.
	 * @param initialId The initial icon id.
	 * @param iconMap The map of ids to icons to use.
	 */
	public IconHelper(Iconic source, String initialId,
			Map<String, ImageData> iconMap) {
		this.source = source;
		lastEvent = new IconEvent(source, initialId);
		this.iconMap = iconMap;
	}
	
	/**
	 * Return an ImageIcon for the given id.
	 * 
	 * @param iconId The id.
	 * @return The ImageIcon never null.
	 */
	@Override
	public ImageData iconForId(String iconId) {
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
		IconListener[] listenersCopy;
		
		synchronized (listeners) {
			if (iconId.equals(lastEvent.getIconId())) {
				return;
			}
			
			lastEvent = localEvent;
			listenersCopy = listeners.toArray(
					new IconListener[0]);
		}
		for (IconListener iconListener : listenersCopy) {
			iconListener.iconEvent(localEvent);
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

	public static ImageIcon imageIconFrom(ImageData imageData) {
		Objects.requireNonNull(imageData);

		try {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData.getBytes()));

			if (image == null) {
				throw new NullPointerException("Buffered Image for " + imageData.getDescription() +
						" is null!");
			}
			return new ImageIconStable(image, imageData.getDescription());
		} catch (IOException e) {
			throw new OddjobWrapperException(e);
		}
	}
}
