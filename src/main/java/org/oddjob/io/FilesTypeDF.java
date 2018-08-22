/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.io;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.IndexedDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class FilesTypeDF implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new FilesDesign(element, parentContext);
	}
}

class FilesDesign extends DesignValueBase {

	private final SimpleTextAttribute files;
	
	private final IndexedDesignProperty list;
	
	public FilesDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		files = new SimpleTextAttribute(
				"files", this);
		
		list = new IndexedDesignProperty(
				"list", this);
	}
	
	public Form detail() {
		return new StandardForm(this).addFormItem(
				new BorderedGroup("Files")
				.add(files.view().setTitle("File Spec"))
				.add(list.view().setTitle("Files List")));
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { files, list };
	}
}
