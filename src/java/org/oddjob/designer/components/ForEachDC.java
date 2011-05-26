/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class ForEachDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ForEachDesign(element, parentContext);
	}
		
}


class ForEachDesign extends BaseDC {
	
	private final SimpleDesignProperty values;
	
	private final SimpleDesignProperty configuration;
	
	ForEachDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		values = new SimpleDesignProperty(
				"values", this);

		configuration = new SimpleDesignProperty(
				"configuration", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
				.addFormItem(basePanel())
				.addFormItem(
				new BorderedGroup(null)
					.add(values.view().setTitle("Values"))
					.add(configuration.view().setTitle("Configuration"))
				);
	}
	
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { name, values, configuration };
	}
}
