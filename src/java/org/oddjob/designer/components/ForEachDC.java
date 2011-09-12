/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
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
public class ForEachDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ForEachDesign(element, parentContext);
	}
		
}


class ForEachDesign extends BaseDC {
	
	private final SimpleDesignProperty values;
	
	private final FileAttribute file;
	
	private final SimpleDesignProperty configuration;
	
	private final SimpleTextAttribute parallel;
	
	private final SimpleDesignProperty executorService;
	
	private final SimpleTextAttribute preLoad;
	
	private final SimpleTextAttribute purgeAfter;
	
	ForEachDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		values = new SimpleDesignProperty(
				"values", this);

		file = new FileAttribute("file", this);
		
		configuration = new SimpleDesignProperty(
				"configuration", this);
		
		parallel = new SimpleTextAttribute(
				"parallel", this);
		
		executorService = new SimpleDesignProperty(
				"executorService", this);
		
		preLoad = new SimpleTextAttribute("preLoad", this);
		
		purgeAfter = new SimpleTextAttribute("purgeAfter", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
				.addFormItem(basePanel())
				.addFormItem(new BorderedGroup("For Each Of")
					.add(values.view().setTitle("Values")))
				.addFormItem(new BorderedGroup("Configuration Options").add(
						new FieldSelection()
							.add(file.view().setTitle("Configuration File"))
							.add(configuration.view().setTitle("Configuration"))
						))
				.addFormItem(
					new TabGroup()
						.add(new FieldGroup("Parallel")
							.add(parallel.view().setTitle("Parallel"))
							.add(executorService.view().setTitle("Execution Service")))
						.add(new FieldGroup("Execution Window")
							.add(preLoad.view().setTitle("Pre-Load"))
							.add(purgeAfter.view().setTitle("Purge After"))))
				;
	}
	
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { name, values, file, configuration, 
				parallel, executorService, preLoad, purgeAfter };
	}
}
