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
public class JMXServiceDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new JMXServiceDesign(element, parentContext);
	}
		
}

class JMXServiceDesign extends BaseDC {

	private final SimpleTextAttribute connection;
	
	private final SimpleDesignProperty environment;
	
	
	public JMXServiceDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		connection = new SimpleTextAttribute("connection", this);
		
		environment = new SimpleDesignProperty("environment", this);
		
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { name, connection, environment };
	}
	
	
	public Form detail() {
		return new StandardForm(this)
		.addFormItem(basePanel())	
		.addFormItem(
				new BorderedGroup("Connection Details")
				.add(connection.view().setTitle("Connection"))
				.add(environment.view().setTitle("Environment"))
			)
		;
	}
		
}

