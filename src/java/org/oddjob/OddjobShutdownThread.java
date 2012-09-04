package org.oddjob;

/**
 * A marker class so jobs know they are being stopped by the a shutdown hook.
 * This is an issue for Explorer that would otherwise do a 
 * <code>frame.dispose()</code> that
 * 
 * @author rob
 *
 */
public abstract class OddjobShutdownThread extends Thread {

}
