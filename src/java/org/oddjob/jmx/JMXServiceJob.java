package org.oddjob.jmx;

import java.io.IOException;

import javax.management.MBeanServerConnection;

import org.oddjob.Structural;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.jmx.general.DomainNode;
import org.oddjob.jmx.general.MBeanDirectory;
import org.oddjob.jmx.general.SimpleDomainNode;
import org.oddjob.jmx.general.SimpleMBeanSession;
import org.oddjob.script.InvokeJob;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * @oddjob.description Expose a JMX Server so that Oddjob jobs
 * can interact with it.
 * <p>
 * Features of this service include:
 * <ul>
 * <li>Attributes of MBeans can be read and changed.</li>
 * <li>Operations on MBeans can be invoked.</li>
 * <li>MBeans are displayed as part of Oddjob's job hierarchy within 
 * Oddjob Explorer.</li>
 * </ul>
 * 
 * MBeans are identified as part of Oddjob's property expansion
 * syntax using their full Object Names. If this service is given
 * the id 'my-jmx-world' an MBean in the domain 'mydomain' and name
 * 'type=greeting,name=hello' would be identified from another Oddjob
 * job with the expression:
 *  
 * <pre>
 * ${my-jmx-world/mydomain:type=greeting,name=hello}
 * </pre>
 * 
 * Note that what is being referenced here is an Oddjob wrapper around
 * the MBean that allows operations and attributes of the MBean to accessed
 * elsewhere. What is referenced is not an MBean instance.
 * <p>
 * The example below shows an MBean (wrapper) being passed as the source 
 * property to an {@link InvokeJob}.
 * <p>
 * Attributes of the MBean can be accessed as if they were properties of 
 * the MBean. If the MBean above has an attribute 'FullText' its value
 * can be accessed using the expression: 
 * 
 * <pre>
 * ${my-jmx-world/mydomain:type=greeting,name=hello.FullText}
 * </pre>
 * 
 * If an MBean Object Name contains dots (.) it must be quoted using double
 * quotes. If the domain in the above example was my.super.domain the 
 * MBean can be identified with the expression:
 * 
 * <pre>
 * ${my-jmx-world/"my.super.domain:type=greeting,name=hello"}
 * </pre>
 * 
 * and the attribute with:
 * 
 * <pre>
 * ${my-jmx-world/"my.super.domain:type=greeting,name=hello".FullText}
 * </pre>
 * 
 * Note that this support for quoting does not apply to Oddjob property
 * expansion expressions in general - only too these MBean identifiers. 
 * 
 * @oddjob.example
 * 
 * This example demonstrates reading an attribute, setting an attribute
 * and invoking an operation.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/JMXServiceExample.xml}
 * 
 * 
 * @author Rob Gordon
 */
public class JMXServiceJob extends ClientBase
implements Structural, BeanDirectoryOwner {
		
	/** Child helper */
	private ChildHelper<DomainNode> childHelper = 
			new ChildHelper<DomainNode>(this);
	
	
	private BeanDirectory beanDirectory;
	
	@Override
	protected void doStart(MBeanServerConnection mbsc) throws IOException {
		
		SimpleMBeanSession session = new SimpleMBeanSession(
				getArooaSession(), mbsc);
		
		String[] domains = mbsc.getDomains();
		
		for (String domain: domains) {
			
			DomainNode node = new SimpleDomainNode(domain, session);
			
			childHelper.addChild(node);
			
			// done after add to allow logger archiver to be added.
			node.initialise();
		}
		
		beanDirectory = new MBeanDirectory(session);
	}

	@Override
	protected void onStop(WhyStop why) {

		while (childHelper.size() > 0) {
			DomainNode node = childHelper.removeChildAt(0);
			node.destroy();
		}
		
		this.beanDirectory = null;
		
	}
		
	@Override
	public BeanDirectory provideBeanDirectory() {
		return beanDirectory;
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.Structural#addStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	@Override
	public void addStructuralListener(StructuralListener listener) {
		childHelper.addStructuralListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.Structural#removeStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	@Override
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}
	
}
