package org.oddjob.state;

import org.oddjob.Structural;

/**
 * Implementation of a {@link StateOperator} that provides a parent state
 * as follows:
 * <ul>
 * <li>If any child is EXCEPTION then evaluate to EXCEPTION.</li>
 * <li>If any child is INCOMPLETE then evaluate to INCOMPLETE.</li>
 * <li>If any child is ACTIVE/EXECUTING then evaluate to ACTIVE.</li>
 * <li>If any child is READY then evaluate to READY.</li>
 * <li>Evaluate to COMPLETE.</li>
 * </ul>
 * 
 * This Operator is legacy behaviour and has been replace by 
 * {@link AnyActiveStateOp} in most {@link Structural} jobs.
 * 
 * @author rob
 *
 */
public class WorstStateOp extends ScoringStateOperator {

	private static final int[] order =
			{
					1, 	// READY
					-1, // EXECUTING
					3, 	// ACTIVE
					2, 	// STARTED
					4, 	// INCOMPLETE
					0, 	// COMPLETE
					5, 	// EXCEPTION
					-1, // DESTROYED
			};

	private final ParentStateConverter parentStateConverter;
	
	public WorstStateOp() {
		this(new StandardParentStateConverter());
	}
	
	public WorstStateOp(ParentStateConverter parentStateConverter) {
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
