/*
 * (c) Rob Gordon 2015.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TextField;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class RunJobDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new RunJobDesign(element, parentContext);
	}
		
}

class RunJobDesign extends BaseDC {

	private final SimpleTextAttribute job;
	
	private final SimpleTextAttribute reset;
	
	public RunJobDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		job = new SimpleTextAttribute("job", this);
		reset = new SimpleTextAttribute("reset", this);
	}

	public DesignProperty[] children() {
		return new DesignProperty[] { name, job, reset };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(
					new BorderedGroup("Job Details")
					.add(new TextField("Job", job))
					.add(new TextField("Reset", reset))
					);
	}
		
}

