package org.oddjob.state;

import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;
import org.oddjob.io.ExistsJob;

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
 * See also the {@link org.oddjob.jobs.job.ResetJob} job.
 * 
 * @oddjob.example
 * 
 * Examples elsewhere.
 * <ul>
 *  <li>See the {@link EqualsState} example. The resets are 
 *  required because retry only sends a soft reset on retry
 *  and it must be hardened to reset the {@link ExistsJob}.
 *  </li>
 * </ul>
 * 
 * @author Rob Gordon
 */
public class Resets extends StructuralJob<Object>{ 
	private static final long serialVersionUID = 2009032400L;

	/**
	 * @oddjob.property
	 * @oddjob.description Harden soft resets. True/False.
	 * @oddjob.required No, defaults to false.
	 */
	private boolean harden;
	
	/**
	 * @oddjob.property
	 * @oddjob.description Soften hard resets. True/False
	 * @oddjob.required No, defaults to false.
	 */
	private boolean soften;
	
	@Override
	protected StateOperator getInitialStateOp() {
		return new AnyActiveStateOp();
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
