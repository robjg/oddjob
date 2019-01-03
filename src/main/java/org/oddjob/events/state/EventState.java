package org.oddjob.events.state;

import org.oddjob.events.EventSource;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.State;
import org.oddjob.state.StateFlag;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Encapsulate the allowed states for a {@link EventSource}.
 * 
 * @author Rob Gordon
 */
public enum EventState implements State {
	
	/**
	 * The Node is ready to be started. 
	 */	
	READY(EnumSet.of(StateFlag.READY)),

	/**
	 * The node is connecting to it's event source.
	 */	
	CONNECTING(EnumSet.of(StateFlag.EXECUTING, StateFlag.STOPPABLE)),

	/**
	 * A node is waiting for its first event.
	 */	
	WAITING(EnumSet.of(StateFlag.STOPPABLE)),
	
	/**
	 * A node is receiving an event.
	 */	
	FIRING(EnumSet.of(StateFlag.STOPPABLE)),
	
	/**
	 * An event has arrived but we are still waiting for more.
	 */	
	TRIGGERED(EnumSet.of(StateFlag.STOPPABLE, StateFlag.COMPLETE)),
	
	/**
	 * The node has stopped subscribing but didn't receive an event.
	 */
	INCOMPLETE(EnumSet.of(StateFlag.INCOMPLETE)),
	
	/**
	 * The node has stopped subscribing but did receive an event.
	 */	
	COMPLETE(EnumSet.of(StateFlag.COMPLETE)),
	
	/**
	 * Indicates an exception has occurred. 
	 */	
	EXCEPTION(EnumSet.of(StateFlag.EXCEPTION)),
	
	/**
	 * The job has been destroyed. It can no longer be used.
	 */	
	DESTROYED(EnumSet.of(StateFlag.DESTROYED)),
	;

	/*
	 * Register Icons for these states.
	 */
	static {
		StateIcons.register(EventState.READY, IconHelper.STARTABLE);
		StateIcons.register(EventState.CONNECTING, IconHelper.EXECUTING);
		StateIcons.register(EventState.WAITING, IconHelper.WAITING);
		StateIcons.register(EventState.FIRING, IconHelper.FIRING);
		StateIcons.register(EventState.TRIGGERED, IconHelper.TRIGGERED);
		StateIcons.register(EventState.INCOMPLETE, IconHelper.NOT_COMPLETE);
		StateIcons.register(EventState.COMPLETE, IconHelper.COMPLETE);
		StateIcons.register(EventState.EXCEPTION, IconHelper.EXCEPTION);
		StateIcons.register(EventState.DESTROYED, IconHelper.INVALID);
	}

	private final Set<StateFlag> flags;

	EventState(EnumSet<StateFlag> flags) {
		this.flags = Collections.unmodifiableSet(flags);
	}

	@Override
	public boolean isReady() {
		return flags.contains(StateFlag.READY);
	}

	@Override
	public boolean isExecuting() {
		return flags.contains(StateFlag.EXECUTING);
	}

	@Override
	public boolean isStoppable() {
		return flags.contains(StateFlag.STOPPABLE);
	}

	@Override
	public boolean isComplete() {
		return flags.contains(StateFlag.COMPLETE);
	}

	@Override
	public boolean isIncomplete() {
		return flags.contains(StateFlag.INCOMPLETE);
	}

	@Override
	public boolean isException() {
		return flags.contains(StateFlag.EXCEPTION);
	}

	@Override
	public boolean isDestroyed() {
		return flags.contains(StateFlag.DESTROYED);
	}
}

