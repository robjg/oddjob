package org.oddjob.tools.taglet;

import java.util.Map;

import org.oddjob.doclet.CustomTagNames;

import com.sun.tools.doclets.Taglet;

/**
 * Taglet for the oddjob.description tag.
 * 
 * @author rob
 *
 */
public class DescriptionTaglet extends BaseBlockTaglet {

	public static void register(Map<String, Taglet> tagletMap) {
	    tagletMap.put(CustomTagNames.DESCRIPTION_TAG_NAME, new DescriptionTaglet());
	}
	
	@Override
	public boolean inType() {
		return true;
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
	public String getName() {
		return CustomTagNames.DESCRIPTION_TAG_NAME;
	}

	@Override
	public String getTitle() {
		return "Description";
	}
}
