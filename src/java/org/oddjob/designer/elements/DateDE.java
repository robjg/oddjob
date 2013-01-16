/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.elements;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 * Design for {@link DateType}
 */
public class DateDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new DateDesign(element, parentContext);
	}
}

class DateDesign extends DesignValueBase {

	private final SimpleTextAttribute date;
	private final SimpleTextAttribute format;
	private final SimpleTextAttribute timeZone;
	private final SimpleDesignProperty clock;
	
	public DateDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		date = new SimpleTextAttribute("date", this);
		format = new SimpleTextAttribute("format", this);
		timeZone = new SimpleTextAttribute("timeZone", this);
		clock = new SimpleDesignProperty("clock", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup("Date")				
			.add(date.view().setTitle("Date"))
			.add(format.view().setTitle("Format"))
			.add(timeZone.view().setTitle("Time Zone"))
			.add(clock.view().setTitle("Clock"))
		);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { date, format, timeZone, clock };
	}
}
