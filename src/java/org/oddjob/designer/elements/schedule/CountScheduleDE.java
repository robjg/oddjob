/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.elements.schedule;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;


/**
 * Count Schedule.
 *
 */

public class CountScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new CountScheduleDesign(element, parentContext);
	}
}

class CountScheduleDesign extends ParentSchedule {
	
	private final SimpleTextAttribute count;
	
	public CountScheduleDesign(ArooaElement element, ArooaContext context) {
		super(element, context);
		
		count = new SimpleTextAttribute("count", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup()
			.add(count.view().setTitle("Count"))
			.add(getRefinement().view().setTitle("Refinement"))
			);
	}
	
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { count, getRefinement() };
	}
	
}