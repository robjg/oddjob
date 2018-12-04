package org.oddjob.state;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * A State made up of {@link StateFlag}s.
 */
public class GenericState implements State {

    private final Set<StateFlag> flags;

    public GenericState(EnumSet<StateFlag> flags) {
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
