package org.oddjob.jmx.server;

import org.oddjob.Structural;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * The anchor bean onto which all server side state is created. 
 * 
 * @author rob
 *
 */
public class ServerMainBean 
implements BeanDirectoryOwner, Structural {

	private final Object root;
	
	private final ChildHelper<Object> childHelper = 
		new ChildHelper<Object>(this);
	
	private final BeanDirectory beanDirectory;
	
	public ServerMainBean(Object root, 
			BeanDirectory beanDirectory) {
		this.root = root;
		this.childHelper.insertChild(0, root);
		this.beanDirectory = beanDirectory;
	}
	
	public BeanDirectory provideBeanDirectory() {
		return new BeanDirectory() {
			
			public Object lookup(String path) 
			throws ArooaPropertyException{
				return beanDirectory.lookup(path);
			}
			
			public <T> T lookup(String path, Class<T> required)
			throws ArooaPropertyException, ArooaConversionException {
				return beanDirectory.lookup(path, required);
			}
			
			public <T> Iterable<T> getAllByType(Class<T> type) {
				return beanDirectory.getAllByType(type);
			}
			
			public String getIdFor(Object bean) {
				return beanDirectory.getIdFor(bean);
			}			
		};
	}
	
	public void addStructuralListener(StructuralListener listener) {
		childHelper.addStructuralListener(listener);
	}
	
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " for [" + root + "]";
	}
}
