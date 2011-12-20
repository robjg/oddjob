package org.oddjob.tools.taglet;

import java.util.Map;

import org.oddjob.doclet.CustomTagNames;

import com.sun.tools.doclets.Taglet;

/**
 * Taglet for the oddjob.property tag.
 * 
 * @author rob
 *
 */
public class PropertyTaglet extends BaseBlockTaglet {

	public static void register(Map<String, Taglet> tagletMap) {
	    tagletMap.put(CustomTagNames.PROPERTY_TAG_NAME, new PropertyTaglet());
	}
	
	@Override
	public boolean inField() {
		return true;
	}

	@Override
	public boolean inMethod() {
		return true;
	}
	
	@Override
	public boolean inType() {
		return false;
	}
	
	@Override
	public String getName() {
		return CustomTagNames.PROPERTY_TAG_NAME;
	}

	@Override
	public String getTitle() {
		return "Property";
	}
}
