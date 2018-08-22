package org.oddjob.jmx;

import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.registry.ServerId;

public class RemoteRegistryCrawler {

	private final BeanDirectory registry;
	
	public RemoteRegistryCrawler(BeanDirectory registry) {
		this.registry = registry;
	}
	
	/**
	 * Helper function to convert a path for a different server into the
	 * path to that servers client from this registery.
	 * 
	 * @param selected The current path being built up in this recursive call.
	 * @param serverId The server id for this current path.
	 * 
	 * @return The path relative to the other server or null if the other server
	 * is not accessible.
	 */
	public BeanDirectory registryForServer(ServerId serverId) {
		if (registry instanceof RemoteDirectory) {
			if (((RemoteDirectory) registry).getServerId().equals(serverId)) {
				return (RemoteDirectory) registry;
			}
			if (ServerId.local().equals(serverId)) {
				return null;
			}
		}
		else {
			if (ServerId.local().equals(serverId)) {
				return registry;
			}
		}
		
		// go round all the child registries recursing down until we find a
		// registry for the server.
		for (BeanDirectoryOwner owner : registry.getAllByType(BeanDirectoryOwner.class)) {
			
			BeanDirectory child = owner.provideBeanDirectory();
			if (child == null) {
				continue;
			}
			
			RemoteRegistryCrawler next = new RemoteRegistryCrawler(child);
			BeanDirectory result = next.registryForServer(serverId);
			
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	/**
	 * Find the object from the possible addresses that could identify it. If
	 * there are several paths to the object (which can happen if the original object
	 * is accessible via several servers), the object with the shortest path is
	 * returned.
	 * 
	 * @param addresses The possible addresses of the component.
	 * @return The component or null if none of the addresses are accessible.
	 * 
	 * @throws ArooaPropertyException 
	 */
	public Object objectForAddress(Address address) throws ArooaPropertyException {
		if (address == null) {
			return null;
		}
		BeanDirectory registry = registryForServer(address.getServerId());
		if (registry == null) {
				return null;
		}
		return registry.lookup(address.getPath().toString());
	}
	
	Address addressFor(Object component, Path path) {

		if (registry instanceof RemoteDirectory) {
			return null;
		}
		
		String id = registry.getIdFor(component);
		if (id != null) {
			return new Address(ServerId.local(), path.addId(id));
		}
		
		for (BeanDirectoryOwner owner: registry.getAllByType(BeanDirectoryOwner.class)) {
			
			String childId = registry.getIdFor(owner);
			if (childId == null) {
				continue;
			}
			
			BeanDirectory child = owner.provideBeanDirectory();
			
			if (child == null) {
				continue;
			}

			Address result = new RemoteRegistryCrawler(
					child).addressFor(component, path);
			
			if (result != null) {
				return result;
			}
		}
		
		return null;
	}
	
	public Address addressFor(Object component) {

		if (component instanceof RemoteOddjobBean) {
			return ((RemoteOddjobBean) component).serverInfo().getAddress();
		}
		return addressFor(component, new Path());
	}
	
}
