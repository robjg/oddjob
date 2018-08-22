package org.oddjob.values.types;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 * Design for {@link TokenizerType}
 */
public class TokenizerTypeDF implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new TokenizerDesign(element, parentContext);
	}
}

class TokenizerDesign extends DesignValueBase {

	private final SimpleTextAttribute text;
	private final SimpleTextAttribute delimiter;
	private final SimpleTextAttribute regexp;
	private final SimpleTextAttribute escape;
	private final SimpleTextAttribute quote;
	
	public TokenizerDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		text = new SimpleTextAttribute("text", this);
		delimiter = new SimpleTextAttribute("delimiter", this);
		regexp = new SimpleTextAttribute("regexp", this);
		escape = new SimpleTextAttribute("escape", this);
		quote = new SimpleTextAttribute("quote", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(new FieldGroup()				
				.add(text.view().setTitle("Text")))
			.addFormItem(new BorderedGroup("Delimiter Properties")
				.add(delimiter.view().setTitle("Delimiter"))
				.add(regexp.view().setTitle("Reg Exp"))
				.add(escape.view().setTitle("Escape"))
				.add(quote.view().setTitle("Quote"))
		);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] 
				{ text, delimiter, regexp, escape, quote };
	}
}
