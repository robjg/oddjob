/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class EmptyDC extends BaseDC {

	public EmptyDC(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { name };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel());
	}
			
}

