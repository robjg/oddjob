package org.oddjob.state;



/**
 * @oddjob.description
 * 
 * A job who's return state is a logical OR of the child states.
 * <p>
 * 
 * @oddjob.example
 * 
 * COMPLETE if either files exist, INCOMPLETE otherwise.
 * 
 * <pre>
 * &lt;or:and xmlns:state="http://rgordon.co.uk/oddjob/state"&gt;
 *  &lt;jobs&gt;
 *   &lt;exists file="doesntexist1"&gt;/>
 *   &lt;exists file="doesntexist2"&gt;/>
 *  &lt;/jobs&gt;
 * &lt;/or:and&gt;
 * </pre>
 * 
 * @author Rob Gordon
 */
public class OrState extends StateReflector {
	private static final long serialVersionUID = 2009031800L;
	
	@Override
	protected StateOperator getStateOp() {
		return new OrStateOp();
	}
	
}
