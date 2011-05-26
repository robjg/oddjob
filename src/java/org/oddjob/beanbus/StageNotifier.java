package org.oddjob.beanbus;

public interface StageNotifier {

	public void addStageListener(StageListener listener);
	
	public void removeStageListener(StageListener listener);
	
}
