package org.oddjob.jmx.general;

import javax.management.MBeanServerConnection;

import org.oddjob.arooa.ArooaSession;

/**
 * A simple implementation of an {@link MBeanSession}.
 * 
 * @author rob
 *
 */
public class SimpleMBeanSession implements MBeanSession {

	private final ArooaSession arooaSession;
	
	private final MBeanCache mBeanCache;
	
	public SimpleMBeanSession(ArooaSession arooaSession,
			MBeanServerConnection mBeanServer) {
		this.arooaSession = arooaSession;
		this.mBeanCache = new MBeanCacheMap(
				mBeanServer, 
				getArooaSession().getArooaDescriptor().getClassResolver());
	}
	
	@Override
	public ArooaSession getArooaSession() {
		return arooaSession;
	}

	@Override
	public MBeanCache getMBeanCache() {
		return mBeanCache;
	}

}
