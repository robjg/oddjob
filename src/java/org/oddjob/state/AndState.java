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
 * <pre>
 * &lt;state:and xmlns:state="http://rgordon.co.uk/oddjob/state"&gt;
 *  &lt;jobs&gt;
 *   &lt;exists file="doesntexist1"&gt;/>
 *   &lt;exists file="doesntexist2"&gt;/>
 *  &lt;/jobs&gt;
 * &lt;/state:and&gt;
 * </pre>
 * 
 * @author Rob Gordon
 */
public class AndState extends StateReflector {
	private static final long serialVersionUID = 2010082000L;
		
	@Override
	protected StateOperator getStateOp() {
		return new AndStateOp();
	}
}
