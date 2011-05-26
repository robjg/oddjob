package org.oddjob.beanbus;

import java.util.EventListener;

public interface StageListener extends EventListener {

	public void stageStarting(StageEvent event);
	
	public void stageComplete(StageEvent event);
}
