/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.MappedDesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.SimpleTextProperty;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TabGroup;
import org.oddjob.arooa.design.screem.TextField;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class ExecDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ExecDesign(element, parentContext);
	}
		
}

class ExecDesign extends BaseDC {

	private final SimpleTextProperty command;
	private final SimpleDesignProperty args;
	private final SimpleTextAttribute dir;
	
	private final SimpleTextAttribute newEnvironment;
	private final MappedDesignProperty environment;
	
	private final SimpleTextAttribute redirectStderr;
	private final SimpleDesignProperty stdin;
	private final SimpleDesignProperty stdout;
	private final SimpleDesignProperty stderr;
	
	public ExecDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);

		command = new SimpleTextProperty("command");
		
		args = new SimpleDesignProperty(
				"args", this);
		
		dir = new SimpleTextAttribute("dir", this);

		newEnvironment = new SimpleTextAttribute("newEnvironement", this);
		
		environment = new MappedDesignProperty(
				"environment", this);

		redirectStderr = new SimpleTextAttribute("redirectStderr", this);		
		
		stdin = new SimpleDesignProperty(
				"stdin", this);
		
		stdout = new SimpleDesignProperty(
				"stdout", this);
		
		stderr = new SimpleDesignProperty(
				"stderr", this);
		
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] {
			name, dir, command, args, newEnvironment, environment, 
			redirectStderr, stdin, stdout, stderr };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(
				new TabGroup() 
					.add(new FieldGroup("Command Details")
						.add(new FieldSelection()
							.add(command.view().setTitle("Command"))
							.add(args.view().setTitle("Args")))
						.add(new TextField("Working Directory", dir))
					)			
					.add(new FieldGroup("Environment")
						.add(newEnvironment.view().setTitle("New Environemnt"))
						.add(environment.view().setTitle("Variables"))
					)
					.add(new FieldGroup("I/O")
						.add(redirectStderr.view().setTitle("Redirect Stderr"))
						.add(stdin.view().setTitle("Stdin"))
						.add(stdout.view().setTitle("Stdout"))
						.add(stderr.view().setTitle("Stderr"))
					)
				);
	}
		
}
