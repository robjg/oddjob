/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.io;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.etc.FileAttribute;
import org.oddjob.arooa.design.screem.FileSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class FileTypeDF implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new FileTypeDesign(element, parentContext);
	}
}

class FileTypeDesign extends DesignValueBase {

	private FileAttribute file;
	
	public FileTypeDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		file = new FileAttribute("file", this);
	}
	
	public Form detail() {
		return new FileSelection("File", file);
	}
	
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { file };
	}
}
