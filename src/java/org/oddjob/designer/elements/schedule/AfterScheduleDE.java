/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.elements.schedule;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;



/**
 * After Schedule.
 *
 */
public class AfterScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new AfterScheduleDesign(element, parentContext);
	}
}

class AfterScheduleDesign extends ParentSchedule {

	private final SimpleDesignProperty schedule;
	

	public AfterScheduleDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		schedule = new SimpleDesignProperty(
				"schedule", this);
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { getRefinement(), schedule };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(new BorderedGroup()
				.add(schedule.view().setTitle("Schedule"))
				.add(getRefinement().view().setTitle("Refinement"))
			);
	}
	
}