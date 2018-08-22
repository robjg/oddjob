/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.elements.schedule;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;



/**
 * Last Schedule.
 *
 */
public class LastScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new LastScheduleDesign(element, parentContext);
	}
}

class LastScheduleDesign extends ParentSchedule {

	public LastScheduleDesign(ArooaElement element, ArooaContext context) {
		super(element, context);
	}
	
	public String toString() {
		return "Last";
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { getRefinement() };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(new BorderedGroup("Properties")
				.add(getRefinement().view().setTitle("Refinement")));
	}
	
}