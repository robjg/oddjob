/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.elements.schedule;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;


/**
 * Broken Schedule.
 *
 */
public class BrokenScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new BrokenScheduleDesign(element, parentContext);
	}
}

class BrokenScheduleDesign extends DesignValueBase {
	
	private final SimpleDesignProperty schedule;
	private final SimpleDesignProperty breaks;
	
	public BrokenScheduleDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		schedule = new SimpleDesignProperty(
				"schedule", this);
		
		breaks = new SimpleDesignProperty(
				"breaks", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup(toString())
			.add(schedule.view().setTitle("Schedule"))
			.add(breaks.view().setTitle("Breaks"))
			);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { schedule, breaks };
	}
	
	public String toString() {
		return "Broken";
	}
	
}