package org.oddjob.framework;

import org.oddjob.arooa.beanutils.DynaArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;

public class WrapDynaArooaClass extends DynaArooaClass {

	public WrapDynaArooaClass(WrapDynaClass dynaClass,
			Class<?> forClass) {
		super(dynaClass, forClass);
	}
	
	@Override
	public WrapDynaClass getDynaClass() {
		return (WrapDynaClass) super.getDynaClass();
	}
	
	@Override
	public BeanOverview getBeanOverview(PropertyAccessor accessor) {
		return new WrapDynaBeanOverview(getDynaClass());
	}
}
