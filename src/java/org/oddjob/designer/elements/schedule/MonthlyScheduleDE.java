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
 * Day of Month Schedule.
 *
 */
public class MonthlyScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new MonthlyScheduleDesign(element, parentContext);
	}
}

class MonthlyScheduleDesign extends ParentSchedule {
	
	private final SimpleTextAttribute onDay;
	private final SimpleTextAttribute fromDay;
	private final SimpleTextAttribute toDay;
				
	private final SimpleTextAttribute inWeek;
	private final SimpleTextAttribute fromWeek;
	private final SimpleTextAttribute toWeek;
	
	private final SimpleTextAttribute onDayOfWeek;
	private final SimpleTextAttribute fromDayOfWeek;
	private final SimpleTextAttribute toDayOfWeek;
	
	public MonthlyScheduleDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		onDay = new SimpleTextAttribute("onDay", this);
		
		fromDay = new SimpleTextAttribute("fromDay", this);
		
		toDay = new SimpleTextAttribute("toDay", this);
		
		inWeek = new SimpleTextAttribute("inWeek", this);
		
		fromWeek = new SimpleTextAttribute("fromWeek", this);
		
		toWeek = new SimpleTextAttribute("toWeek", this);
		
		onDayOfWeek = new SimpleTextAttribute("onDayOfWeek", this);
		
		fromDayOfWeek = new SimpleTextAttribute("fromDayOfWeek", this);
		
		toDayOfWeek = new SimpleTextAttribute("toDayOfWeek", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(new BorderedGroup("Day")
				.add(new FieldSelection()
						.add(new FieldGroup()
							.add(fromDay.view().setTitle("From"))
								.add(toDay.view().setTitle("To")))
							.add(onDay.view().setTitle("On"))))
			.addFormItem(new BorderedGroup("Week")
				.add(new FieldSelection()
						.add(new FieldGroup()
							.add(fromWeek.view().setTitle("From"))
								.add(toWeek.view().setTitle("To")))
							.add(inWeek.view().setTitle("In"))))
			.addFormItem(new BorderedGroup("Day Of Week")
				.add(new FieldSelection()
						.add(new FieldGroup()
							.add(fromDayOfWeek.view().setTitle("From"))
								.add(toDayOfWeek.view().setTitle("To")))
							.add(onDayOfWeek.view().setTitle("On"))))
			.addFormItem(new BorderedGroup()
				.add(getRefinement().view().setTitle("Refinement"))
		);				
	}
			
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { onDay, fromDay, toDay, 
				inWeek, fromWeek, toWeek,
				onDayOfWeek, fromDayOfWeek, toDayOfWeek,
				getRefinement() };
	}
}