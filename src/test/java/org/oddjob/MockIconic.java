package org.oddjob;

import org.oddjob.images.IconListener;
import org.oddjob.images.ImageData;

public class MockIconic implements Iconic {

	public ImageData iconForId(String id) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	public void addIconListener(IconListener listener) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}

	public void removeIconListener(IconListener listener) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
}
