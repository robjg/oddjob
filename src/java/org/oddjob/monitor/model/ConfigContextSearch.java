package org.oddjob.monitor.model;

import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.monitor.context.AncestorSearch;
import org.oddjob.monitor.context.ExplorerContext;

/**
 * Provides an {@link ExplorerContext} search for Edit Actions
 * and Design Actions. 
 * 
 * @author rob
 *
 */
public class ConfigContextSearch {

	/**
	 * Find the appropriate configuration session.
	 * 
	 * @param context The context. Must not be null.
	 * 
	 * @return The session, or null if one can't be found for the 
	 * current context. This will be the case for an Oddjob that hasn't
	 * been loaded, for instance.
	 */
	public ConfigurationSession sessionFor(ExplorerContext context) {

		ConfigurationOwner configOwner = null; 
		
		if (context.getParent() == null) {
			configOwner = 
				(ConfigurationOwner) context.getValue(
						ConfigContextInialiser.CONFIG_OWNER);
		}
		else {
			// Use the parent node so Oddjob nodes can be copied.
			AncestorSearch search = new AncestorSearch(context.getParent());
			configOwner = 
				(ConfigurationOwner) search.getValue(
						ConfigContextInialiser.CONFIG_OWNER);
		}
		
		if (configOwner != null) {
			return configOwner.provideConfigurationSession();
		}
		return null;
	}
	
	/**
	 * Provide the appropriate {@link DragPoint}.
	 * 
	 * @param context
	 * @return
	 */
	public DragPoint dragPointFor(ExplorerContext context) {
		
		ConfigurationSession session = sessionFor(context);

		if (session != null) {
			return session.dragPointFor(context.getThisComponent());
		}
		
		return null;
	}
	
}
