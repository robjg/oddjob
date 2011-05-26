package org.oddjob;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;

public class OddjobLookup {

	private final BeanDirectoryOwner dirOwner;
	
	public OddjobLookup(BeanDirectoryOwner owner) {
		this.dirOwner = owner;
	}
	
	public Object lookup(String fullPath) 
	throws ArooaPropertyException {
		BeanDirectory directory = dirOwner.provideBeanDirectory();
		if (directory == null) {
			return null;
		}
		
		return directory.lookup(fullPath);
	}
	
	public <T> T lookup(String fullPath, Class<T> type) 
	throws ArooaPropertyException, ArooaConversionException {
		BeanDirectory directory = dirOwner.provideBeanDirectory();
		if (directory == null) {
			return null;
		}
		
		return directory.lookup(fullPath, type);
	}
}
