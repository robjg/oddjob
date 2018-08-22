/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jobs;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TabGroup;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;
import org.oddjob.sql.SQLJob;

/**
 * {@link DesignFactory} for {@link SQLJob}
 */
public class CheckJobDesFa implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new CheckDesign(element, parentContext);
	}
}

class CheckDesign extends BaseDC {

	private final SimpleTextAttribute value;
	
	private final SimpleTextAttribute eq;
	
	private final SimpleTextAttribute ne;
	
	private final SimpleTextAttribute lt;
	
	private final SimpleTextAttribute le;
	
	private final SimpleTextAttribute gt;
	
	private final SimpleTextAttribute ge;
	
	private final SimpleTextAttribute _null;
	
	private final SimpleTextAttribute z;
	
	public CheckDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);

		value = new SimpleTextAttribute("value", this);
		
		eq = new SimpleTextAttribute("eq", this);
		
		ne = new SimpleTextAttribute("ne", this);
		
		lt = new SimpleTextAttribute("lt", this);
		
		le = new SimpleTextAttribute("le", this);
		
		gt = new SimpleTextAttribute("gt", this);
		
		ge = new SimpleTextAttribute("ge", this);
		
		_null = new SimpleTextAttribute("null", this);
		
		z = new SimpleTextAttribute("z", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(new BorderedGroup("Value")
				.add(value.view().setTitle("Value To Check"))
				)
			.addFormItem(
				new TabGroup()
					.add(new FieldGroup("Comparisons")
						.add(eq.view().setTitle("Equal To"))
						.add(ne.view().setTitle("Not Equal To"))
						.add(lt.view().setTitle("Less Than"))
						.add(le.view().setTitle("Less or Equal To"))
						.add(gt.view().setTitle("Greater Than"))
						.add(ge.view().setTitle("Greater or Equal To"))
					)
					.add(new FieldGroup("Others")
						.add(_null.view().setTitle("Is Null"))
						.add(z.view().setTitle("Zero Length"))
					)
				);					
	}
			
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { name, value, eq, ne,
				lt, le, gt, ge, _null, z };
	}
}
