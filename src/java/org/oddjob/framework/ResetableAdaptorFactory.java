package org.oddjob.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.oddjob.Resetable;
import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.reflect.PropertyAccessor;

/**
 * Create an adaptor to a Resetable that adapts a component either
 * because it is Resetable or because it has annotations to resetable
 * methods. 
 * 
 * @author rob
 *
 */
public class ResetableAdaptorFactory {

	/**
	 * Create a resetable.
	 * 
	 * @param component
	 * @param session
	 * 
	 * @return A Resetable. Always creates the resetable even if all
	 * methods are no-ops.
	 */
	public Resetable resetableFor(final Object component, 
			ArooaSession session) {
		if (component instanceof Resetable) {
			return (Resetable) component;
		}
		
		PropertyAccessor accessor = 
				session.getTools().getPropertyAccessor();
		
		ArooaBeanDescriptor beanDescriptor = 
				session.getArooaDescriptor().getBeanDescriptor(
						new SimpleArooaClass(component.getClass()), 
						accessor);
		
		ArooaAnnotations annotations = 
				beanDescriptor.getAnnotations();
		
		final Method softResetMethod = 
				annotations.methodFor(SoftReset.class.getName());
		final Method hardResetMethod = 
				annotations.methodFor(HardReset.class.getName());
		
		return new Resetable() {
			
			@Override
			public boolean softReset() {
				invoke(component, softResetMethod);
				return true;
			}
			
			@Override
			public boolean hardReset() {
				invoke(component, hardResetMethod);
				return true;
			}
		};
	}
	
	private void invoke(Object component, Method m) {
		if (m == null) {
			return;
		}
		try {
			m.invoke(component);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
