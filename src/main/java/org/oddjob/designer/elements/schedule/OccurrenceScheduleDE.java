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
public class OccurrenceScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new OccurrenceScheduleDesign(element, parentContext);
	}
}

class OccurrenceScheduleDesign extends ParentSchedule {
	
	private final SimpleTextAttribute occurrence;
	
	public OccurrenceScheduleDesign(ArooaElement element, ArooaContext context) {
		super(element, context);
		
		occurrence = new SimpleTextAttribute("occurrence", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup(toString())
			.add(occurrence.view().setTitle("Count"))
		    .add(getRefinement().view().setTitle("Refinement"))	
		);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { occurrence, getRefinement() };
	}
	
	public String toString() {
		return "Occurrence " + occurrence.attribute() == null 
				? "" : occurrence.attribute();
	}
	
}