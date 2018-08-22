package org.oddjob.framework;

import org.oddjob.Stoppable;

public interface Service extends Stoppable {

	public void start() throws Exception;
	
}
