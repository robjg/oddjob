package org.oddjob.tools.taglet;

import java.util.Map;

import org.oddjob.doclet.CustomTagNames;

import com.sun.tools.doclets.Taglet;

/**
 * Taglet for the oddjob.example tag.
 * 
 * @author rob
 *
 */
public class ExampleTaglet extends BaseBlockTaglet {

	public static void register(Map<String, Taglet> tagletMap) {
	    tagletMap.put(CustomTagNames.EXAMPLE_TAG_NAME, new ExampleTaglet());
	}
	
	@Override
	public boolean inType() {
		return true;
	}
	
	@Override
	public boolean inField() {
		return false;
	}
	
	@Override
	public boolean inMethod() {
		return false;
	}
	
	@Override
	public String getName() {
		return CustomTagNames.EXAMPLE_TAG_NAME;
	}

	@Override
	public String getTitle() {
		return "Example";
	}
}
