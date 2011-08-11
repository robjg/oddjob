package org.oddjob.state;

import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;

/**
 * @oddjob.description
 * 
 * Runs it's child job and then compares the state of the child job to 
 * the given state. It's own state is complete if the states match, 
 * incomplete otherwise.
 * <p>
 * This job is probably most useful in it's 'not equals' form - i.e. to 
 * check when something hasn't completed.
 * 
 * @oddjob.example
 * 
 * COMPLETE when the child job isn't complete.
 * 
 * &lt;sequential xmlns:state="http://rgordon.co.uk/oddjob/state"&gt;
 *  &lt;jobs&gt;
 *   &lt;state:equals not="true"&gt;
 *    &lt;job&gt;
 *     &lt;exists file="mydb.lck"/&gt;
 *    &lt;/job&gt;
 *   &lt;/state:equals&gt;
 *  &lt;echo text="Backing up the Database..."/&gt;
 *  &lt;/jobs&gt;
 * &lt;/sequential&gt;
 * 
 * @author Rob Gordon
 */
public class EqualsState extends StructuralJob<Stateful>
implements Stoppable {
	private static final long serialVersionUID = 2009031800L;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The state to match.
	 * @oddjob.required No, defaults to COMPLETE.
	 */
	private StateCondition state = StateConditions.COMPLETE;
			
	public StateCondition getState() {
		return state;
	}
	
	@ArooaAttribute
	public void setState(StateCondition state) {
		this.state = state;
	}		
	
		
	@Override
	protected StateOperator getStateOp() {
		return new StateOperator() {
			public ParentState evaluate(State... states) {
				if (states.length == 0) {
					return ParentState.READY;
				}

				State state = states[0];
				
				if (EqualsState.this.state.test(state)) {
					return ParentState.COMPLETE;
				}
				else {
					return ParentState.INCOMPLETE;
				}
			}
		};
	}
	
	/**
	 * @oddjob.property job
	 * @oddjob.description The job to run who's state will be compared.
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public synchronized void setJob(Stateful job) {
		if (job == null) {
			childHelper.removeChildAt(0);
		}
		else {
			childHelper.insertChild(0, job);
		}
	}

	@Override
	protected void execute() throws Throwable {
		Stateful job = childHelper.getChild();

		if (job == null) {
			throw new NullPointerException("No Job.");
		}

		if (job instanceof Runnable) {
			((Runnable) job).run();
		}
	}
	
}
