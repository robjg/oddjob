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
public class RenameDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new RenameDesign(element, parentContext);
	}
		
}

class RenameDesign extends BaseDC {

	private final SimpleTextAttribute from;
	private final SimpleTextAttribute to;
	
	public RenameDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		from = new SimpleTextAttribute("from", this);
		to = new SimpleTextAttribute("to", this);
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { name, from , to };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(
					new BorderedGroup("File Details")
					.add(new TextField("From", from))
					.add(new TextField("To", to)));
	}
	
}
