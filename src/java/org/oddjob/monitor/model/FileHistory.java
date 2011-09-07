package org.oddjob.monitor.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileHistory implements Serializable {

	private static final long serialVersionUID = 2011090600L;
	
	private transient List<Runnable> changeActions;
	
	private int listSize = 4;
	
	private List<File> fileHistory = new ArrayList<File>();
	
	public synchronized void addChangeAction(Runnable action) {
		if (changeActions == null) {
			changeActions = new ArrayList<Runnable>();
		}
		changeActions.add(action);
	}
	
	public synchronized void removeChangeAction(Runnable action) {
		if (changeActions == null) {
			return;
		}
		changeActions.remove(action);
	}

	public synchronized void addHistory(File file) {
		fileHistory.remove(file);
		fileHistory.add(file);
		while (fileHistory.size() > listSize) {
			fileHistory.remove(0);
		}

		List<Runnable> copyActions = new ArrayList<Runnable>(
				changeActions);
		
		for (Runnable action : copyActions) {
			action.run();
		}		
	}
	
	public int size() {
		return fileHistory.size();
	}

	public File get(int i) {
		return fileHistory.get(i);
	}
	
	public int getListSize() {
		return listSize;
	}
	
	public void setListSize(int listSize) {
		this.listSize = listSize;
	}

}
