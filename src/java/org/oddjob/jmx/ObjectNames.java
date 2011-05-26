package org.oddjob.jmx;

import javax.management.ObjectName;

public interface ObjectNames {

	ObjectName nameFor(Object object);
	
	Object objectFor(ObjectName objectName);
}
