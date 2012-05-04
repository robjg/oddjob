package org.oddjob.jmx.client;

import org.oddjob.state.State;

public class ClientDestroyed implements State {

	@Override
	public boolean isReady() {
		return false;
	}
	@Override
	public boolean isStoppable() {
		return false;
	}
	@Override
	public boolean isPassable() {
		return false;
	}
	@Override
	public boolean isComplete() {
		return false;
	}
	@Override
	public boolean isIncomplete() {
		return false;
	}
	@Override
	public boolean isException() {
		return false;
	}
	@Override
	public boolean isDestroyed() {
		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
