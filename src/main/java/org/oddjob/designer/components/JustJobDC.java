/*
 * (c) Rob Gordon 2005.
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
public class JustJobDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new JustJobDesign(element, parentContext);
	}
		
}

class JustJobDesign extends BaseDC {

	private final SimpleTextAttribute job;
	
	public JustJobDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		job = new SimpleTextAttribute("job", this);
	}

	public DesignProperty[] children() {
		return new DesignProperty[] { name, job };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(
					new BorderedGroup("Job Details")
					.add(new TextField("Job", job)));
	}
		
}

