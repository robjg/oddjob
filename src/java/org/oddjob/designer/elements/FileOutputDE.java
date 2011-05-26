/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.elements;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.etc.FileAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.io.FileType;

/**
 * The {@link DesignFactory} for a {@link FileType}.
 */
public class FileOutputDE implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new FileDesign(element, parentContext);
	}
}

class FileOutputDesign extends DesignValueBase {

	private final FileAttribute file;
	
	private final SimpleTextAttribute append;
	
	public FileOutputDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		file = new FileAttribute("file", this);
		
		append = new SimpleTextAttribute(
				"append", this);
		
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup("Files")
				.add(file.view().setTitle("File"))
				.add(append.view().setTitle("Append")));
	}
	
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { file, append };
	}
}
