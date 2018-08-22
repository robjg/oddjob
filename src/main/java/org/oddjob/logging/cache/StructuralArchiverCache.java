/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.Structural;
import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * A Cache for Log Events. This cache tracks changes to the structure of
 * the component tree and adds and removes LogArchives.
 * <p>
 * 
 * @author Rob Gordon.
 */
public class StructuralArchiverCache extends AbstractArchiverCache {
	
	/** Structural listener */
	private final StructuralListener structuralListener = new StructuralListener() {
		public void childAdded(StructuralEvent event) {
			Object node = event.getChild();
			addChild(node);
		}
		public void childRemoved(StructuralEvent event) {
			Object node = event.getChild();
			removeChild(node);
		}
	};
	
	/** List of nodes we're listening to so we can tidy up. */
	private final List<Structural> listeningTo = 
		new ArrayList<Structural>();

	
	/**
	 * Construct a LogArchiver archiving message from the given root object
	 * and all it's children.
	 * 
	 * @param root The root object.
	 * @param resolver A reslover which resolves archive name, either locally
	 * or using a remote url.
	 */
	public StructuralArchiverCache(Object root, ArchiveNameResolver resolver) {
		this(root, LogArchiver.MAX_HISTORY, resolver);
	}
		
	/**
	 * Construct a LogArchiver archiving message from the given root object
	 * and all it's children with the given amount of history.
	 * 
	 * @param root The root object.
	 * @param maxHistory The number of lines to store for each logger
	 * @param resolver A reslover which resolves archive name, either locally
	 * or using a remote url.
	 */
	public StructuralArchiverCache(Object root, int maxHistory, ArchiveNameResolver resolver) {
		super(resolver, maxHistory);
		addChild(root);
	}

	/**
	 * Add a child node to this Log Archiver.
	 * 
	 * @param node The child node.
	 */
	void addChild(Object node) {
		addArchive(node);
		
		if (node instanceof LogArchiver) {
			return;
		}
		
		if (node instanceof Structural) {
			((Structural) node).addStructuralListener(structuralListener);
			listeningTo.add( (Structural) node);
		}
	}	

	/**
	 * Remove a child node and possibly it archive.
	 * @param node
	 */
	void removeChild(Object node) {
		removeArchive(node);
				
		if (node instanceof Structural) {
			((Structural) node).removeStructuralListener(structuralListener);
			listeningTo.remove(node);
		}
	}
	
	public void destroy() {
		while (listeningTo.size() > 0) {
			removeChild(listeningTo.get(0));
		}
	}
}
