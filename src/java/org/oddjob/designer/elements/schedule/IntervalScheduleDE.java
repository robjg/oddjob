/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.elements.schedule;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;


/**
 * Interval Schedule.
 *
 */
public class IntervalScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new IntervalScheduleDesign(element, parentContext);
	}
}

class IntervalScheduleDesign extends DesignValueBase {
	
	private final SimpleTextAttribute interval;
		
	public IntervalScheduleDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		interval = new SimpleTextAttribute("interval", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup(toString())
			.add(interval.view().setTitle("Interval")));
	}				

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { interval } ;
	}
	
	public String toString() {
		return "Interval";
	}
	
}