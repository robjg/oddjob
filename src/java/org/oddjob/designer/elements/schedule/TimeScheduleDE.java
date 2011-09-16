/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.elements.schedule;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 * Time Schedule
 *
 */
public class TimeScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new TimeScheduleDesign(element, parentContext);
	}
}

class TimeScheduleDesign extends ParentSchedule {
	
	private final SimpleTextAttribute at;
	private final SimpleTextAttribute from;
	private final SimpleTextAttribute to;

	public TimeScheduleDesign(ArooaElement element, ArooaContext context) {
		super(element, context);
		
		at = new SimpleTextAttribute("at", this);
		
		from = new SimpleTextAttribute("from", this);
		
		to = new SimpleTextAttribute("to", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(new BorderedGroup()
				.add(new FieldSelection()
					.add(new FieldGroup()
						.add(from.view().setTitle("From"))
						.add(to.view().setTitle("To")))
					.add(at.view().setTitle("At"))))
			.addFormItem(new BorderedGroup()
				.add(getRefinement().view().setTitle("Refinement")));				
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { at, from, to, getRefinement() };
	}
}