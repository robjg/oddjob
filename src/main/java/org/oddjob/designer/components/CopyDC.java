/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.etc.FileAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;


/**
 *
 */
public class CopyDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new CopyDesign(element, parentContext);
	}
		
}

class CopyDesign extends BaseDC {

	private final SimpleDesignProperty from;

	private final FileAttribute to;

	private final SimpleDesignProperty input;

	private final SimpleDesignProperty output;

	private final SimpleDesignProperty consumer;

	public CopyDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		from = new SimpleDesignProperty(
				"from", this);

		to = new FileAttribute("to", this);
		
		input = new SimpleDesignProperty(
				"input", this);		
		
		output = new SimpleDesignProperty(
				"output", this);

		consumer = new SimpleDesignProperty(
				"consumer", this);
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { name, from, to, input, output, consumer };
	}
	
	public Form detail() {
		return 
			new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(new BorderedGroup("From")
					.add(new FieldSelection()
						.add(from.view().setTitle("File(s)/Dir(s)"))
						.add(input.view().setTitle("Input"))))
			.addFormItem(new BorderedGroup("To")
					.add(new FieldSelection()
							.add(to.view().setTitle("File/Dir"))
							.add(output.view().setTitle("Output"))
							.add(consumer.view().setTitle("Consumer"))));
	}
	
}
