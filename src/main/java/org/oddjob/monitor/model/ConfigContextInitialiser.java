package org.oddjob.monitor.model;

import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;

/**
 * A {@link ContextInitialiser} for {@link ConfigurationOwner}.
 * 
 * @author rob
 *
 */
public class ConfigContextInitialiser implements ContextInitialiser {

	/** The key. */
	public static String CONFIG_OWNER = "configOwner";
	
	private final ExplorerModel explorerModel;
	
	/**
	 * Constructor.
	 * 
	 * @param explorerModel
	 */
	public ConfigContextInitialiser(ExplorerModel explorerModel) {
		this.explorerModel = explorerModel;
	}
	
	public void initialise(ExplorerContext context) {

		ConfigurationOwner configOwner = null;
		
		ExplorerContext parent = context.getParent();
		
		if (parent == null) {
			if (context.getThisComponent() != explorerModel.getOddjob()) {
				throw new RuntimeException("If this exception isn't throw we can refactor out getOddjob.");
			}
			configOwner = explorerModel.getOddjob();
		}
		else {
			if (context.getThisComponent() instanceof ConfigurationOwner) {
				configOwner = 
					(ConfigurationOwner) context.getThisComponent();
			}
		}
		
		if (configOwner != null) {
			context.setValue(CONFIG_OWNER, configOwner);
		}
	}
}
