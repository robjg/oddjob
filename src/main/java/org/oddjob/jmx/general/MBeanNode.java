package org.oddjob.jmx.general;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.jmx.client.Destroyable;
import org.oddjob.script.Invoker;

/**
 * Marker interface for the wrapper around an MBean.
 * 
 * @author rob
 *
 */
public interface MBeanNode extends DynaBean, Invoker, Destroyable {

	/**
	 * Called from the {@link DomainNode} to initialise this node.
	 */
	void initialise();
}
 