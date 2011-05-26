package org.oddjob.jmx.server;

import javax.management.MBeanOperationInfo;

import org.oddjob.jmx.RemoteOperation;

abstract public class JMXOperation<T> extends RemoteOperation<T> {

	abstract public MBeanOperationInfo getOpInfo();

}
