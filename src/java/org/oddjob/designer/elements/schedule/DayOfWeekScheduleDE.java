/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.elements.schedule;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;



/**
 * Day of Week Schedule.
 *
 */
public class DayOfWeekScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new DayOfWeekScheduleDesign(element, parentContext);
	}
}

class DayOfWeekScheduleDesign extends ParentSchedule {
	
	private final SimpleTextAttribute on;
	private final SimpleTextAttribute from;
	private final SimpleTextAttribute to;

	public DayOfWeekScheduleDesign(ArooaElement element, ArooaContext context) {
		super(element, context);
		
		on = new SimpleTextAttribute("on", this);
		
		from = new SimpleTextAttribute("from", this);
		
		to = new SimpleTextAttribute("to", this);
		
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup(toString())
			.add(new FieldSelection()
					.add(new BorderedGroup()
						.add(from.view().setTitle("From"))
						.add(to.view().setTitle("To")))
					.add(on.view().setTitle("On"))
					)
			.add(getRefinement().view().setTitle("Refinement"))
		);				
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { on, from, to, getRefinement() };
	}
}