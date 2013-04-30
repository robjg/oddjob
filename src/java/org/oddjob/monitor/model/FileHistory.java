package org.oddjob.monitor.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to Track File history between multiple versions of Oddjob
 * Explorer.
 * <p>
 * File history size isn't parameterised via a property but maybe
 * it should.
 * 
 * @author rob
 *
 */
public class FileHistory implements Serializable {
	
	private static final long serialVersionUID = 2011090600L;
	
	public static final int DEFAULT_FILE_HISTORY_SIZE = 9;
	
	private transient List<Runnable> changeActions;
	
	private volatile int listSize = DEFAULT_FILE_HISTORY_SIZE;
	
	private final List<File> fileHistory = new ArrayList<File>();
	
	/**
	 * Add an action to be taken when the file menu changes, such as
	 * updating the File menu.
	 * 
	 * @param action The action.
	 */
	public synchronized void addChangeAction(Runnable action) {
		if (changeActions == null) {
			changeActions = new ArrayList<Runnable>();
		}
		changeActions.add(action);
	}
	
	/**
	 * Remove an action.
	 * 
	 * @param action
	 */
	public synchronized void removeChangeAction(Runnable action) {
		if (changeActions == null) {
			return;
		}
		changeActions.remove(action);
	}

	/**
	 * Add a file to the history.
	 * 
	 * @param file
	 */
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
