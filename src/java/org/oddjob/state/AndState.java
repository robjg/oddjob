package org.oddjob.state;



/**
 * @oddjob.description
 * 
 * A job who's return state is a logical AND of the child states.
 * <p>
 * 
 * @oddjob.example
 * 
 * COMPLETE if both files exist, INCOMPLETE otherwise.
 * 
 * {@oddjob.xml.resource org/oddjob/state/AndStateExample.xml}
 * 
 * @author Rob Gordon
 */
public class AndState extends StateReflector {
	private static final long serialVersionUID = 2010082000L;
		
	@Override
	protected StateOperator getInitialStateOp() {
		return new AndStateOp();
	}
}
