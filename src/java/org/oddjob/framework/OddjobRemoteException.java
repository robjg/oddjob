/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

public class OddjobRemoteException extends Exception {
	private static final long serialVersionUID = 20051115; 
	
	public OddjobRemoteException(String msg) {
		super(msg);
	}
	
	public OddjobRemoteException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
