/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.elements;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
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
public class ConnectionDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ConnectionDesign(element, parentContext);
	}
}

class ConnectionDesign extends DesignValueBase {

	private final SimpleTextAttribute driver = 
		new SimpleTextAttribute("driver", this);
	
	private final SimpleTextAttribute url = 
		new SimpleTextAttribute("url", this);
	
	private final SimpleTextAttribute username = 
		new SimpleTextAttribute("username", this);
	
	private final SimpleTextAttribute password = 
		new SimpleTextAttribute("password", this);

	private final SimpleDesignProperty classLoader = 
		new SimpleDesignProperty("classLoader", this);


	public ConnectionDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
	}
	
	
	public DesignProperty[] children() {
		return new DesignProperty[] { driver, url, username, password, 
				classLoader };
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup("Connection Details")				
				.add(driver.view().setTitle("Driver"))
				.add(url.view().setTitle("Url"))
				.add(username.view().setTitle("Username"))
				.add(password.view().setTitle("Password"))
				.add(classLoader.view().setTitle("Class Loader"))
		);
	}

}
