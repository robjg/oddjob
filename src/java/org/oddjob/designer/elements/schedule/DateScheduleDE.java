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
 * A Date schedule.
 *
 */
public class DateScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new DateScheduleDesign(element, parentContext);
	}
}

class DateScheduleDesign extends ParentSchedule {
	
	private final SimpleTextAttribute on; 
	private final SimpleTextAttribute from;
	private final SimpleTextAttribute to;
			
	public DateScheduleDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		on = new SimpleTextAttribute(
				"on", this);
		
		from = new SimpleTextAttribute(
				"from", this);
		
		to = new SimpleTextAttribute(
				"to", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup("Properties")
			.add(new FieldSelection()
					.add(new BorderedGroup()
						.add(from.view().setTitle("From"))
						.add(to.view().setTitle("To")))
					.add(on.view().setTitle("On")))
			.add(getRefinement().view().setTitle("Refinement"))
		);				
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { on, from, to, getRefinement()};
	}
}