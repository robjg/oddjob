package org.oddjob.state;

import java.io.Serializable;
import java.util.*;

/**
 * A State made up of {@link StateFlag}s.
 */
public class GenericState implements State, Serializable {

    private static final long serialVersionUID = 2020070900L;

    private final String name;

    private final EnumSet<StateFlag> flags;

    public GenericState(String name, Collection<StateFlag> flags) {
        this.name = Objects.requireNonNull(name);
        this.flags = EnumSet.copyOf(flags);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericState that = (GenericState) o;
        return name.equals(that.name) &&
                flags.equals(that.flags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, flags);
    }

    @Override
    public String toString() {
        return name;
    }

    public static boolean statesEquivalent(State first, State second) {
        return from(first).flags.equals(from(second).flags);
    }

    public static GenericState from(State state) {

        if (state instanceof GenericState) {
            return (GenericState) state;
        }
        List<StateFlag> flags = new ArrayList<>(8);
        if (state.isReady()) {
            flags.add(StateFlag.READY);
        }
        if (state.isExecuting()) {
            flags.add(StateFlag.EXECUTING);
        }
        if (state.isStoppable()) {
            flags.add(StateFlag.STOPPABLE);
        }
        if (state.isComplete()) {
            flags.add(StateFlag.COMPLETE);
        }
        if (state.isIncomplete()) {
            flags.add(StateFlag.INCOMPLETE);
        }
        if (state.isException()) {
            flags.add(StateFlag.EXCEPTION);
        }
        if (state.isDestroyed()) {
            flags.add(StateFlag.DESTROYED);
        }

        return new GenericState(state.toString(), flags);
    }


}
