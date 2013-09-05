package org.oddjob.script;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.IndexedDesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;

/**
 *
 */
public class InvokeJobDF implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new InvokeJobDesign(element, parentContext);
	}
		
}

class InvokeJobDesign extends BaseDC {

	private final SimpleDesignProperty source;
	private final SimpleTextAttribute function;
	
	private final SimpleDesignProperty args;
	private final IndexedDesignProperty parameters;
	
	
	public InvokeJobDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);

		source = new SimpleDesignProperty(
				"source", this);
		
		function = new SimpleTextAttribute("function", this);
		
		args = new SimpleDesignProperty(
				"args", this);
		
		parameters = new IndexedDesignProperty(
				"parameters", this);

	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] {
			name, source, function, args, parameters };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(
				new FieldGroup("Target")
					.add(source.view().setTitle("Source"))
					.add(function.view().setTitle("Function")))
			.addFormItem(new FieldGroup("Arguments")
						.add(new FieldSelection()
							.add(args.view().setTitle("Args"))
							.add(parameters.view().setTitle("Parameters")))
				);
	}
		
}
