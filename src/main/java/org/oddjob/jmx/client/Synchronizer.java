package org.oddjob.jmx.client;

import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;

import java.util.LinkedList;

/**
 * Synchronises asynchronous notifications with a synchronous class to
 * get initial state.
 * <p>
 * During the synchronisation phase any asynchronous events are queued and
 * processed after synchronisation. Duplicates are detected by the
 * notification number and removed.
 * 
 * @author rob
 *
 */
public class Synchronizer implements NotificationListener {

	private final NotificationListener listener;
	
	private LinkedList<Notification> pending = new LinkedList<>();
	
	public Synchronizer(NotificationListener listener) {
		this.listener = listener;
	}
	
	public void handleNotification(Notification notification) {
		synchronized(this) {
			if (pending != null) {
				pending.addLast(notification);
				return;
			}
		}
		listener.handleNotification(notification);
	}

	/**
	 * Synchronous synchronisation with notifications.
	 * 
	 * @param last The last notifications.
	 */
	public void synchronize(Notification[] last) {
		long seq = 0;
		for (Notification notification : last) {
			listener.handleNotification(notification);
			seq = notification.getSequenceNumber();
		}
		
		while (true) {
			Notification notification;
			synchronized (this) {
				if (pending.isEmpty()) {
					pending = null;
					return;
				}
				notification = pending.removeFirst();
				if (notification.getSequenceNumber() < seq) {
					continue;
				}
			}
			listener.handleNotification(notification);
		}
	}	
}
