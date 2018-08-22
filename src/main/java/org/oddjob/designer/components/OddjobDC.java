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
import org.oddjob.arooa.design.etc.FileAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TabGroup;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class OddjobDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new OddjobDesign(element, parentContext);
	}
}

class OddjobDesign extends BaseDC {

	private final FileAttribute file;
	
	private final SimpleDesignProperty configuration;

	private final SimpleDesignProperty args;
	
	private final MappedDesignProperty export;
	
	private final SimpleDesignProperty properties;
	
	private final SimpleTextAttribute inheritance;
	
	private final SimpleDesignProperty descriptorFactory;
	
	private final SimpleDesignProperty classLoader;
	
	private final SimpleDesignProperty persister;
	
	OddjobDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		file = new FileAttribute("file", this);
		
		configuration = new SimpleDesignProperty("configuration", this);
		
		args = new SimpleDesignProperty("args", this);

		export = new MappedDesignProperty("export", this);
		
		properties = new SimpleDesignProperty("properties", this);
		
		inheritance = new SimpleTextAttribute("inheritance", this);
		
		descriptorFactory = new SimpleDesignProperty("descriptorFactory", this);
		
		classLoader = new SimpleDesignProperty("classLoader", this);
		
		persister = new SimpleDesignProperty("persister", this);
		
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(new BorderedGroup("Configuration Options").add(
					new FieldSelection()
						.add(file.view().setTitle("Configuration File"))
						.add(configuration.view().setTitle("Configuration"))
					))
			.addFormItem(
				new TabGroup()
					.add(new FieldGroup("Export")
						.add(args.view().setTitle("Arguments"))
						.add(properties.view().setTitle("Properties"))
						.add(inheritance.view().setTitle("Inheritance"))
						.add(export.view().setTitle("Export"))
					)
					.add(new FieldGroup("Advanced")
						.add(descriptorFactory.view().setTitle("Descriptor"))
						.add(classLoader.view().setTitle("ClassLoader"))
						.add(persister.view().setTitle("Persister"))
					)
				);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, 
				file, configuration,
				args, properties, inheritance, export, 
				descriptorFactory, classLoader, persister };
	}
}
