package org.oddjob.monitor.actions;

import java.beans.PropertyChangeListener;

import javax.swing.KeyStroke;

import org.oddjob.monitor.context.ExplorerContext;

public class MockExplorerAction implements ExplorerAction {

	@Override
	public String getName() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public String getGroup() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public Integer getMnemonicKey() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public KeyStroke getAcceleratorKey() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void setSelectedContext(ExplorerContext eContext) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void addPropertyChangeListener(
			PropertyChangeListener listener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void removePropertyChangeListener(
			PropertyChangeListener listener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void prepare() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void action() throws Exception {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public boolean isEnabled() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public boolean isVisible() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
}
