/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class EchoDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new EchoDesign(element, parentContext);
	}
		
}

class EchoDesign extends BaseDC {

	private final SimpleTextAttribute text;
	
	private final SimpleDesignProperty lines;
	
	private final SimpleDesignProperty output;
	
	public EchoDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		text = new SimpleTextAttribute("text", this);
		
		lines = new SimpleDesignProperty("lines", this);
		
		output = new SimpleDesignProperty("output", this);
	}

	public DesignProperty[] children() {
		return new DesignProperty[] { name, text, lines, output };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(
				new BorderedGroup("Text")
					.add(new FieldSelection()
						.add(text.view().setTitle("Text"))
						.add(lines.view().setTitle("Lines"))))
			.addFormItem(
				new BorderedGroup("Output")
					.add(output.view().setTitle("Output"))
			);
	}		
}

