package org.oddjob.jmx.server;

import javax.management.MBeanOperationInfo;

public interface OddjobJMXAccessController {

	boolean isAccessible(MBeanOperationInfo opInfo);
	
}
