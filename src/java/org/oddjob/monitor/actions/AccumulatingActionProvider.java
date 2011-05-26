package org.oddjob.monitor.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link ActionProvider} that provides accumulates 
 * of other <code>ActionProviders<code>s.
 * 
 * @author rob
 *
 */
public class AccumulatingActionProvider implements ActionProvider {

	/** The providers. */
	private List<ActionProvider> providers = 
		new ArrayList<ActionProvider>();	
	
	
	/**
	 * Add a provider.
	 * 
	 * @param provider
	 */
	public void addProvider(ActionProvider provider) {
		providers.add(provider);
	}
	
	public ExplorerAction[] getExplorerActions() {
		List<ExplorerAction> results = 
			new ArrayList<ExplorerAction>();
		
		for (ActionProvider provider : providers ) {
			results.addAll(Arrays.asList(provider.getExplorerActions()));
		}
		
		return results.toArray(new ExplorerAction[results.size()]);
	}	
}
