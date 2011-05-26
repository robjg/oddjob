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
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class ServerDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ServerDesign(element, parentContext);
	}
}

class ServerDesign extends BaseDC {

	private final SimpleTextAttribute root;

	private final SimpleTextAttribute url;
	
	private final SimpleDesignProperty environment;
	
	private final SimpleTextAttribute logFormat;
	
	private final SimpleDesignProperty handlerFactories;
	
	public ServerDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);

		root = new SimpleTextAttribute("root", this);
		
		url = new SimpleTextAttribute("url", this);
		
		environment = new SimpleDesignProperty("environment", this);
		
		logFormat = new SimpleTextAttribute("logFormat", this);
		
		handlerFactories = new SimpleDesignProperty("handlerFactories", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(
				new BorderedGroup("Server Details")
					.add(root.view().setTitle("Root Node"))
					.add(url.view().setTitle("URL"))
					.add(environment.view().setTitle("Environment")))
			.addFormItem(
					new BorderedGroup("Advanced")
						.add(logFormat.view().setTitle("Log Format"))
						.add(handlerFactories.view().setTitle("Handler Factories"))
			);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { name, root, url, environment,
				logFormat, handlerFactories };
	}
}

