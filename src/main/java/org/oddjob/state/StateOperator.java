package org.oddjob.state;

import org.oddjob.Structural;
import org.oddjob.structural.OddjobChildException;

/**
 * An operation that provides the result of evaluating many states. These
 * are used by {@link Structural} jobs to decide their own state.
 * <p>
 * It is illegal to pass the {@link JobState#DESTROYED} state as
 * an argument. Behaviour is undefined in this instance.
 * 
 * @author rob
 *
 */
public interface StateOperator {

	/**
	 * Evaluate the given states.
	 * 
	 * @param states The states.
	 * @return The result state.
 	 */
	StateEvent evaluate(StateEvent... states);

	static StateEvent toParentEvent(StateEvent childEvent, ParentStateConverter parentStateConverter) {
		Throwable childException = childEvent.getException();
		OddjobChildException exception;

		if (childException == null) {
			exception = null;
		}
		else if (childException instanceof OddjobChildException) {
			exception = (OddjobChildException) childException;
		}
		else {
			exception = new OddjobChildException(childException,
					childEvent.getSource().toString());
		}

		return StateEvent.exceptionNow(
				childEvent.getSource(),
				parentStateConverter.toStructuralState(childEvent.getState()),
				exception);
	}

}
