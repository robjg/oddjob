package org.oddjob.jmx.server;

import javax.management.MBeanOperationInfo;

public interface OddjobJMXAccessController {

	public boolean isAccessable(MBeanOperationInfo opInfo);
	
}
