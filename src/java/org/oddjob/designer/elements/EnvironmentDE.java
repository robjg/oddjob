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
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class EnvironmentDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new EnvironmentDesign(element, parentContext);
	}
}

class EnvironmentDesign extends DesignValueBase {

	private SimpleTextAttribute name;
	
	public EnvironmentDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		name = new SimpleTextAttribute("name", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup("Environment")
			.add(name.view().setTitle("Name")));
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { name };
	}
}
