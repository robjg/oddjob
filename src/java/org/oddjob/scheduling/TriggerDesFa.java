/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.scheduling;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.etc.ReferenceAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TextField;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;

/**
 *
 */
public class TriggerDesFa implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new TriggerDesign(element, parentContext);
	}
}

class TriggerDesign extends BaseDC {

	private final ReferenceAttribute on;;
	
	private final SimpleTextAttribute state;

	private final SimpleTextAttribute newOnly;
	
	private final SimpleDesignProperty job;
	
	public TriggerDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		on = new ReferenceAttribute("on", this);
		
		state = new SimpleTextAttribute("state", this);
		
		newOnly = new SimpleTextAttribute("newOnly", this);
		
		job = new SimpleDesignProperty(
				"job", this);
	}

	public DesignProperty[] children() {
		return new DesignProperty[] { name, on, state, newOnly, job };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(
					new BorderedGroup("Trigger Details")
						.add(new TextField("On Job", on))
						.add(new TextField("State", state))
						.add(new TextField("New Only", newOnly))
						.add(job.view().setTitle("Job"))
				);
	}
		
}

