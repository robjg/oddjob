/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.elements;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 * DesignElement for the Format Type.
 */
public class FormatDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new FormatDesign(element, parentContext);
	}
}

class FormatDesign extends DesignValueBase {

	private final SimpleTextAttribute format;
	private final SimpleTextAttribute timeZone;
	private final SimpleTextAttribute date;
	private final SimpleTextAttribute number;
		
	public FormatDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		format = new SimpleTextAttribute("format", this);
		
		timeZone = new SimpleTextAttribute("timeZone", this);

		date = new SimpleTextAttribute("date", this);
		
		number = new SimpleTextAttribute("number", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup(toString())
				.add(format.view().setTitle("Format"))
				.add(new FieldSelection()
						.add(new BorderedGroup("Date Time")
							.add(date.view().setTitle("Date"))
							.add(timeZone.view().setTitle("Time Zone")))
						.add(number.view().setTitle("Number")))
					);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { format, timeZone, date, number };
	}
	
	public String toString() {
		return "Format";
	}
	
}
