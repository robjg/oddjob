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
		
	private final SimpleDesignProperty values;
	private final SimpleDesignProperty until ;
	private final SimpleTextAttribute times;
	
	private final SimpleDesignProperty job;
	
	
	public RepeatDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		values = new SimpleDesignProperty(
				"values", this);
		
		until = new SimpleDesignProperty("until", this);

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
					.add(values.view().setTitle("Values"))
					.add(until.view().setTitle("Until"))
					.add(times.view().setTitle("Times"))
					);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, values, until, times, job };
	}
	
	
	
}
