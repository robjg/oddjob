/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.etc.ReferenceAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class WaitDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new WaitDesign(element, parentContext);
	}
}

class WaitDesign extends BaseDC {

	private final SimpleTextAttribute pause;
	
	private final ReferenceAttribute forProperty;
	
	private final SimpleTextAttribute state;

	public WaitDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		pause = new SimpleTextAttribute("pause", this);
		
		forProperty = new ReferenceAttribute("for", this);
		
		state = new SimpleTextAttribute("state", this);
	}
		
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(
					new BorderedGroup("Properties")
					.add(pause.view().setTitle("Delay"))
					.add(forProperty.view().setTitle("Property"))
					.add(state.view().setTitle("State"))
					);
	}
		
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { name, pause, forProperty, state };
	}
}

