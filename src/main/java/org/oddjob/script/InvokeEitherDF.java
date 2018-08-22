package org.oddjob.script;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.IndexedDesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.FormItem;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;

/**
 * The {@link DesignFactory}s for {@link InvokeJob} and {@link InvokeType}.
 */
public class InvokeEitherDF implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		switch (parentContext.getArooaType()) {
		case COMPONENT:
			return new InvokeJobDesign(element, parentContext);
		case VALUE:
			return new InvokeTypeDesign(element, parentContext);
		}
		throw new IllegalStateException("Unknown Type.");
	}
		
}

class InvokeJobDesign extends BaseDC {
	
	private final InvokeDesignCommon delegate;
	
	public InvokeJobDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		delegate = new InvokeDesignCommon(this);
	}

	@Override
	public Form detail() {
		FormItem[] detail = delegate.detail();
		
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(detail[0])
			.addFormItem(detail[1]);
	}
	
	@Override
	public DesignProperty[] children() {
		DesignProperty[] delegates = delegate.children();
		DesignProperty[] all = new DesignProperty[delegates.length + 1];
		all[0] = name;
		System.arraycopy(delegates, 0, all, 1, delegates.length);
		return all;
	}
}

class InvokeTypeDesign extends DesignValueBase {
	
	private final InvokeDesignCommon delegate;
	
	public InvokeTypeDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		delegate = new InvokeDesignCommon(this);
	}

	@Override
	public Form detail() {
		FormItem[] detail = delegate.detail();
		return new StandardForm(this)
			.addFormItem(detail[0])
			.addFormItem(detail[1]);
	}
	
	@Override
	public DesignProperty[] children() {
		return delegate.children();
	}
}

class InvokeDesignCommon {

	private final SimpleDesignProperty source;
	private final SimpleTextAttribute function;
	
	private final SimpleDesignProperty args;
	private final IndexedDesignProperty parameters;
	
	
	public InvokeDesignCommon(DesignInstance owner) {

		source = new SimpleDesignProperty(
				"source", owner);
		
		function = new SimpleTextAttribute("function", owner);
		
		args = new SimpleDesignProperty(
				"args", owner);
		
		parameters = new IndexedDesignProperty(
				"parameters", owner);

	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] {
			source, function, args, parameters };
	}
	
	FormItem[] detail() {
		return new FormItem[] {
				new FieldGroup("Target")
					.add(source.view().setTitle("Source"))
					.add(function.view().setTitle("Function")),
				new FieldGroup("Arguments")
					.add(new FieldSelection()
						.add(args.view().setTitle("Args"))
						.add(parameters.view().setTitle("Parameters")))
		};
	}		
}
