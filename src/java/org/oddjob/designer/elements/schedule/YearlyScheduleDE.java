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
 * A Date schedule.
 *
 */
public class YearlyScheduleDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new YearlyScheduleDesign(element, parentContext);
	}
}

class YearlyScheduleDesign extends ParentSchedule {
	
	private final SimpleTextAttribute onDate; 
	private final SimpleTextAttribute fromDate;
	private final SimpleTextAttribute toDate;
			
	private final SimpleTextAttribute inMonth;
	private final SimpleTextAttribute fromMonth;
	private final SimpleTextAttribute toMonth;
	
	public YearlyScheduleDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		onDate = new SimpleTextAttribute("onDate", this);
		
		fromDate = new SimpleTextAttribute("fromDate",this);
		
		toDate = new SimpleTextAttribute("toDate", this);
		
		inMonth = new SimpleTextAttribute("inMonth", this);
		
		fromMonth = new SimpleTextAttribute("fromMonth", this);
		
		toMonth = new SimpleTextAttribute("toMonth", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(new BorderedGroup("Date")
				.add(new FieldSelection()
					.add(new FieldGroup()
						.add(fromDate.view().setTitle("From Date"))
						.add(toDate.view().setTitle("To Date")))
					.add(onDate.view().setTitle("On Date"))))
			.addFormItem(new BorderedGroup("Month")
				.add(new FieldSelection()
					.add(new FieldGroup()
						.add(fromMonth.view().setTitle("From Month"))
						.add(toMonth.view().setTitle("To Month")))
					.add(inMonth.view().setTitle("In Month"))))
			.addFormItem(new BorderedGroup()
				.add(getRefinement().view().setTitle("Refinement")));				
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { onDate, fromDate , toDate, 
				inMonth, fromMonth, toMonth, getRefinement() };
	}
	
}