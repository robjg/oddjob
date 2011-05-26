package org.oddjob.monitor.actions;

import java.net.URL;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 * An {@link ActionProvider} that loads {@link ActionProvider}s
 * from XML configuration found at the given URLs.
 * 
 * @author rob
 *
 */
public class URLActionProvider implements ActionProvider {

	/** The URLs. */
	private final URL[] urls;
	
	/** The session for the parser to use. */
	private final ArooaSession session;

	/**
	 * Constructor.
	 * 
	 * @param urls
	 * @param session
	 */
	public URLActionProvider(URL[] urls, ArooaSession session) {
		this.urls = urls;
		this.session = session;
	}
	
	public ExplorerAction[] getExplorerActions() {
		
		if (urls.length == 0) {
			return null;
		}
		
		AccumulatingActionProvider accumulator = 
			new AccumulatingActionProvider();
		
		try {
				
			for (URL url: urls ) {
				
				XMLConfiguration config = new XMLConfiguration(
						url.toString(), url.openStream());
				
				StandardFragmentParser parser = 
					new StandardFragmentParser(session);
				
				parser.parse(config);
				
				ActionProvider provider = 
					(ActionProvider) parser.getRoot();
				
				accumulator.addProvider(provider);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return accumulator.getExplorerActions();
	}
	
}
