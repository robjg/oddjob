/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.values.properties;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.IndexedDesignProperty;
import org.oddjob.arooa.design.MappedDesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.FormItem;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TabGroup;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;

/**
 *
 */
public class PropertiesDesFa implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		switch (parentContext.getArooaType()) {
		case COMPONENT:
			return new PropertiesJobDesign(element, parentContext);
		case VALUE:
			return new PropertiesTypeDesign(element, parentContext);
		}
		throw new IllegalStateException("Unknown Type.");
	}
}


class PropertiesJobDesign extends BaseDC {

	final private PropertiesDesign delegate;
	
	private final SimpleTextAttribute override;
	
	private final SimpleTextAttribute environment;
	
	public PropertiesJobDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		override = new SimpleTextAttribute("override", this);
						
		environment = new SimpleTextAttribute("environment", this);
		
		this.delegate = new PropertiesDesign(element, parentContext, this);	
	}
	
	@Override
	public Form detail() {
		FormItem detail = delegate.detail();
		
		return new StandardForm(this)
		.addFormItem(basePanel())
		.addFormItem(new FieldGroup()
				.add(override.view().setTitle("Override"))
				.add(environment.view().setTitle("Environment"))
				)
		.addFormItem(detail);
	}
	
	@Override
	public DesignProperty[] children() {
		DesignProperty[] delegates = delegate.children();
		DesignProperty[] all = new DesignProperty[delegates.length + 3];
		all[0] = name;
		all[1] = override;
		all[2] = environment;
		System.arraycopy(delegates, 0, all, 3, delegates.length);
		return all;
	}
}

class PropertiesTypeDesign extends DesignValueBase {
	
	final private PropertiesDesign delegate;
	
	public PropertiesTypeDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		this.delegate = new PropertiesDesign(element, parentContext, this);	
	}
	
	@Override
	public Form detail() {
		FormItem detail = delegate.detail();
		return new StandardForm(this).addFormItem(
			detail);
	}
	
	@Override
	public DesignProperty[] children() {
		return delegate.children();
	}
}

/**
 * Shared between the Properties Job and Type.
 *
 */
class PropertiesDesign {

	private final MappedDesignProperty values;
	private final IndexedDesignProperty sets;
	private final SimpleTextAttribute fromXML;
	private final SimpleTextAttribute substitute;
	private final SimpleDesignProperty input;
	private final SimpleTextAttribute extract;
	private final SimpleTextAttribute prefix;
			
	public PropertiesDesign(ArooaElement element, ArooaContext parentContext,
			DesignInstance owner) {
				
		values = new MappedDesignProperty(
				"values", owner);
		
		sets = new IndexedDesignProperty(
				"sets", owner);
		
		substitute = new SimpleTextAttribute(
				"substitute", owner);
		
		fromXML = new SimpleTextAttribute(
				"fromXML", owner);
		
		input = new SimpleDesignProperty(
				"input", owner);
		
		extract = new SimpleTextAttribute("extract", owner);
		
		prefix = new SimpleTextAttribute("prefix", owner);
	}
	
	FormItem detail() {
		return 
			new TabGroup("Properties")
				.add(values.view().setTitle("Values")
				    )
				.add(sets.view().setTitle("Sets")
					)
				.add(new FieldGroup("Input")
					.add(input.view().setTitle("Input"))
					.add(fromXML.view().setTitle("From XML"))
				)
				.add(new FieldGroup("Advanced")
					.add(substitute.view().setTitle("Substitute"))
					.add(extract.view().setTitle("Extract Prefix"))
					.add(prefix.view().setTitle("Prepend Prefix"))
				);
	}

	public DesignProperty[] children() {
		return new DesignProperty[] { 
				extract,
				prefix,
				values, 
				sets,
				fromXML,
				substitute,
				input};
	}
}
