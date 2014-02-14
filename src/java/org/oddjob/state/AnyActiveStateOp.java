package org.oddjob.state;

import static org.oddjob.state.ParentState.*;

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
public class AnyActiveStateOp implements StateOperator {
	
	private static final ParentState[][] STATE_MATRIX = {
		{ READY,      null, ACTIVE,     STARTED,    INCOMPLETE, READY,      EXCEPTION, 	null },
		{ null,       null, null,       null,       null,       null,       null,      	null },
		{ ACTIVE,     null, ACTIVE,     ACTIVE,     ACTIVE, 	ACTIVE,     ACTIVE, 	null },
		{ STARTED,    null, ACTIVE,     STARTED,    STARTED, 	STARTED,    STARTED, 	null },
		{ INCOMPLETE, null, ACTIVE, 	STARTED, 	INCOMPLETE, INCOMPLETE, EXCEPTION, 	null },
		{ READY,      null, ACTIVE,     STARTED,    INCOMPLETE, COMPLETE,   EXCEPTION, 	null },
		{ EXCEPTION,  null, ACTIVE,  	STARTED,  	EXCEPTION, 	EXCEPTION,  EXCEPTION, 	null },
		{ null,       null, null,       null,       null,      null,        null,      	null },
	};
	
	private final ParentStateConverter parentStateConverter;
	
	public AnyActiveStateOp() {
		this(new StandardParentStateConverter());
	}
	
	public AnyActiveStateOp(ParentStateConverter parentStateConverter) {
		this.parentStateConverter = parentStateConverter;
	}
	
	@Override
	public ParentState evaluate(State... states) {
		
		new AssertNonDestroyed().evaluate(states);
		
		ParentState state = ParentState.READY;
		
		if (states.length > 0) {
			
			state = parentStateConverter.toStructuralState(states[0]);
			
			for (int i = 1; i < states.length; ++i) {
				ParentState next = parentStateConverter.toStructuralState(
						states[i]);

				state = STATE_MATRIX[state.ordinal()][next.ordinal()];
			}
		}
		
		return state;
	}
	
	public String toString() {
		
		return getClass().getSimpleName();
	}
}
