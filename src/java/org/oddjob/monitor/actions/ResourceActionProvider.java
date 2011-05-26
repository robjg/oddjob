package org.oddjob.monitor.actions;

import java.net.URL;

import org.oddjob.arooa.ArooaSession;

/**
 * An {@link ActionProvider} that provides actions from 
 * any number of XML Configurations found on the class path.
 * 
 * @author rob
 *
 */
public class ResourceActionProvider implements ActionProvider {

	/** The resource name. */
	public static final String ACTION_FILE = "META-INF/oj-explorer.xml";
	
	/** The session to use for finding resources and parsing
	 * the configurations. */
	private final ArooaSession session;
	
	/**
	 * Constructor.
	 * 
	 * @param session
	 */
	public ResourceActionProvider(ArooaSession session) {
		this.session = session;
	}
	
	public ExplorerAction[] getExplorerActions() {
		
		URL[] urls = session.getArooaDescriptor().getClassResolver().getResources(
				ACTION_FILE);
		
		return new URLActionProvider(urls, session).getExplorerActions();
	}

}
