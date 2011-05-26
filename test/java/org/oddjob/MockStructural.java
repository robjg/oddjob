package org.oddjob;

import org.oddjob.structural.StructuralListener;

public class MockStructural implements Structural {

	public void addStructuralListener(StructuralListener listener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void removeStructuralListener(StructuralListener listener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
