package org.oddjob;

/**
 * A marker class so jobs know they are being stopped by the a shutdown hook.
 * This is an issue for Explorer that would otherwise do a 
 * <code>frame.dispose()</code> it hangs and I don't know why. Answers on
 * a postcard please.
 * 
 * @author rob
 *
 */
public abstract class OddjobShutdownThread extends Thread {

	public OddjobShutdownThread() {
		super(OddjobShutdownThread.class.getName());
	}
}
