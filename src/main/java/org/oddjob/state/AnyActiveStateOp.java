package org.oddjob.state;

import org.oddjob.Structural;

/**
 * Implementation of a {@link StateOperator} that provides a parent state
 * as follows:
 * <ul>
 * <li>If any child is ACTIVE/EXECUTING then evaluate to ACTIVE.</li>
 * <li>If any child is STARTED then evaluate to STARTED.</li>
 * <li>If any child is EXCEPTION then evaluate to EXCEPTION.</li>
 * <li>If any child is INCOMPLETE then evaluate to INCOMPLETE.</li>
 * <li>If any child is READY then evaluate to READY.</li>
 * <li>Evaluate to COMPLETE.</li>
 * </ul>
 * 
 * This Operator is used in many {@link Structural}
 * jobs to calculate parent state. An ACTIVE or STARTED state is returned
 * even if a child has failed because this is necessary to keep Oddjob
 * alive if no other job has started a none daemon thread.
 *  
 * @author rob
 *
 */
public class AnyActiveStateOp extends ScoringStateOperator {

	private static final int[] order =
	{
			1, 	// READY
			-1, // EXECUTING
			5, 	// ACTIVE
			4, 	// STARTED
			2, 	// INCOMPLETE
			0, 	// COMPLETE
			3, 	// EXCEPTION
			-1, // DESTROYED
	};

	private final ParentStateConverter parentStateConverter;
	
	public AnyActiveStateOp() {
		this(new StandardParentStateConverter());
	}
	
	public AnyActiveStateOp(ParentStateConverter parentStateConverter) {
		this.parentStateConverter = parentStateConverter;
	}

	@Override
	protected int score(ParentState state) {
		return order[state.ordinal()];
	}

	@Override
	protected ParentStateConverter getParentStateConverter() {
		return parentStateConverter;
	}

	public String toString() {
		return getClass().getSimpleName();
	}
}
