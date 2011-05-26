/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.etc.ReferenceAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class RepeatDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new RepeatDesign(element, parentContext);
	}
}

class RepeatDesign extends BaseDC {
		
	private final SimpleDesignProperty schedule ;
	private final SimpleDesignProperty retry;
	
	private final SimpleDesignProperty job;
	
	private final ReferenceAttribute exception;
	
	public RepeatDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		schedule = new SimpleDesignProperty(
				"schedule", this);

		retry = new SimpleDesignProperty(
				"retry", this);
		
		job = new SimpleDesignProperty(
				"job", this);
		
		exception = new ReferenceAttribute("exception", this);
	}
	
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(
					new BorderedGroup("Properties")
					.add(job.view().setTitle("Job"))
					.add(schedule.view().setTitle("Schedule"))
					.add(retry.view().setTitle("Retry"))
					.add(exception.view().setTitle("Exception")));
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, schedule, retry, job, exception };
	}
	
	
	
}
