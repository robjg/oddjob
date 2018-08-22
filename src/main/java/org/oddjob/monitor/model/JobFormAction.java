package org.oddjob.monitor.model;

import org.oddjob.arooa.design.screem.Form;
import org.oddjob.monitor.actions.FormAction;

/**
 * Base class for actions that launch a form.
 * 
 * @author rob
 *
 */
abstract public class JobFormAction extends JobAction implements FormAction {

	@Override
	public final Form form() {
		if (checkPrepare()) {
			return doForm();
		} 
		else {
			return null;
		}
		
	}
	
	/**
	 * Override this method to create the form.
	 * 
	 * @return
	 */
	abstract protected Form doForm();

}
