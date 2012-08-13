package org.oddjob;

import javax.swing.ImageIcon;

import org.oddjob.images.IconListener;

public class MockIconic implements Iconic {

	public ImageIcon iconForId(String id) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	public void addIconListener(IconListener listener) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}

	public void removeIconListener(IconListener listener) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
}
