package org.oddjob.state;

import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;

/**
 * 
 * @oddjob.description
 * 
 * Captures Reset actions propagating down a job tree and either hardens
 * soft resets to hard resets or softens hard resets to soft resets before
 * passing them on to the child job.
 * <p>
 * Execute and Stop actions are cascaded as normal to the child job.
 * <p> 
 * 
 * @oddjob.example
 * 
 * See the scheduling example.
 * 
 * @author Rob Gordon
 */
public class Resets extends StructuralJob<Object>{ 
	private static final long serialVersionUID = 2009032400L;

	/**
	 * @oddjob.property
	 * @oddjob.description Harden soft resets.
	 * @oddjob.required No.
	 */
	private boolean harden;
	
	/**
	 * @oddjob.property
	 * @oddjob.description Soften hard resets.
	 * @oddjob.required No, defaults to COMPLETE.
	 */
	private boolean soften;
	
	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}
	
	@Override
	protected void execute() throws Throwable {
		Object job = childHelper.getChild();
		if (job != null && job instanceof Runnable) {
			((Runnable) job).run();
		}
	}

	public boolean isHarden() {
		return harden;
	}

	public void setHarden(boolean harden) {
		this.harden = harden;
	}

	public boolean isSoften() {
		return soften;
	}

	
	public void setSoften(boolean soften) {
		this.soften = soften;
	}

	/**
	 * @oddjob.property job
	 * @oddjob.description The job to pass resets on to.
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public synchronized void setJob(Object job) {
		if (job == null) {
			childHelper.removeChildAt(0);
		}
		else {
			childHelper.insertChild(0, job);
		}
	}
	
	@Override
	public boolean hardReset() {
		if (soften) {
			return super.softReset();
		}
		else {
			return super.hardReset();
		}
	}

	@Override
	public boolean softReset() {
		if (harden) {
			return super.hardReset();
		}
		else {
			return super.softReset();
		}
	}
}
