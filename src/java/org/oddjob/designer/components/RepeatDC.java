/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
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
		
	private final SimpleTextAttribute until ;
	private final SimpleTextAttribute times;
	
	private final SimpleDesignProperty job;
	
	
	public RepeatDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		until = new SimpleTextAttribute("until", this);

		times = new SimpleTextAttribute("times", this);
		
		job = new SimpleDesignProperty(
				"job", this);
	}
	
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(
					new BorderedGroup("Properties")
					.add(job.view().setTitle("Job"))
					.add(until.view().setTitle("Until"))
					.add(times.view().setTitle("Times"))
					);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, until, times, job };
	}
	
	
	
}
