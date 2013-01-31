package org.oddjob.beanbus;

public interface BadBeanHandler<T> {

	public void handle(T originalBean, BadBeanException e) 
	throws BusCrashException;
}
