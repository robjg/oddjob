package org.oddjob.beanbus.mega;


import java.util.Collection;

import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.deploy.annotations.ArooaInterceptor;
import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.GenericDesignFactory;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationOwnerSupport;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.ContextConfigurationSession;
import org.oddjob.arooa.parsing.OwnerStateListener;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusService;
import org.oddjob.beanbus.BusServiceProvider;
import org.oddjob.beanbus.Outbound;
import org.oddjob.beanbus.SimpleBusService;
import org.oddjob.framework.StructuralJob;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.StateOperator;

/**
 *
 * @oddjob.description A job that allows the construction of a 
 * {@link BeanBus}.
 * <p>
 * A Bean Bus is an assembly of {@link Collection}s.
 * 
 * 
 * @oddjob.example
 * 
 * A simple bus example.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/mega/MegaBeanBusExample.xml}
 * 
 * 
 * @author Rob Gordon
 * 
 */
@ArooaInterceptor("org.oddjob.beanbus.mega.MegaBeanBusInterceptor")
public class MegaBeanBus extends StructuralJob<Object>
implements ConfigurationOwner, BusServiceProvider {
	
    private static final long serialVersionUID = 2012021500L;
	
	/** Support for configuration modification. */
	private transient ConfigurationOwnerSupport configurationOwnerSupport;
	
	private transient volatile BusConductor busConductor;
	
	private boolean noAutoLink;
	
	/**
	 * Only constructor.
	 */
	public MegaBeanBus() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		configurationOwnerSupport =
			new ConfigurationOwnerSupport(this);		
	}
	
	@Override
	@ArooaHidden
	public void setArooaContext(ArooaContext context) {
		super.setArooaContext(context);
		
		configurationOwnerSupport.setConfigurationSession(
				new ContextConfigurationSession(context));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.arooa.parsing.ConfigurationOwner#provideConfigurationSession()
	 */
	@Override
	public ConfigurationSession provideConfigurationSession() {
		
		return configurationOwnerSupport.provideConfigurationSession();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.arooa.parsing.ConfigurationOwner#addOwnerStateListener(org.oddjob.arooa.parsing.OwnerStateListener)
	 */
	@Override
	public void addOwnerStateListener(OwnerStateListener listener) {
		configurationOwnerSupport.addOwnerStateListener(listener);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.arooa.parsing.ConfigurationOwner#removeOwnerStateListener(org.oddjob.arooa.parsing.OwnerStateListener)
	 */
	@Override
	public void removeOwnerStateListener(OwnerStateListener listener) {
		configurationOwnerSupport.removeOwnerStateListener(listener);
	}
	
	@Override
	public DesignFactory rootDesignFactory() {
		return new GenericDesignFactory(new SimpleArooaClass(
				this.getClass()));
	}
	
	@Override
	public ArooaElement rootElement() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.framework.StructuralJob#getStateOp()
	 */
	@Override
	protected StateOperator getInitialStateOp() {
		return new AnyActiveStateOp();
	}

	/**
	 * Add a child.
	 * 
	 * @oddjob.property jobs
	 * @oddjob.description The child jobs.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param child A child
	 */
	@ArooaComponent
	public void setParts(int index, Object child) {
		if (child == null) {
			childHelper.removeChildAt(index);
		}
		else {
			childHelper.insertChild(index, child);
		}
	}		
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void execute() throws Exception {
		
		Object[] children = childHelper.getChildren();

		StatefulBusConductorAdapter adaptor = null;
		
		Object previousChild = null;
		
		try {
			for (Object child : children) {
				
				if (child instanceof BusServiceProvider) {
					busConductor = ((BusServiceProvider) child).getServices(
							).getService(SimpleBusService.BEAN_BUS_SERVICE_NAME);
				}
	
				if (busConductor == null && adaptor == null && 
						child instanceof Stateful) {
					adaptor = new StatefulBusConductorAdapter(
							(Stateful) child);
					busConductor = adaptor;
				}
				
				if (child instanceof BusPart) {
					
					// We need to identify the bus conductor before the 
					// first bus part.
					if (busConductor == null) {
						throw new IllegalStateException("No Bus Conductor!");
					}
					
					((BusPart) child).prepare(busConductor);
				}
				
				if (!noAutoLink && previousChild != null && 
						child instanceof Collection) {
					
					Outbound outbound = new OutboundStrategies(
							).outboundFor(previousChild, getArooaSession());
					
					if (outbound != null) {
						outbound.setTo((Collection) child);
					
						logger().info("Automatically Linked Outbound [" + 
							previousChild + "] to [" + child + "]");
					}
				}
				
				previousChild = child;
			}
			
			
			for (Object child : children) {
				
				if (child instanceof Runnable) {
					((Runnable) child).run();
				}
			}
		}
		finally {
			if (adaptor != null) {
				adaptor.close();
			}
			busConductor = null;
		}		
	}	

	@Override
	protected void onReset() {
		super.onReset();
		busConductor = null;
	}
	
	@Override
	public BusService getServices() {
		return new BusService() {
			
			@Override
			public String serviceNameFor(Class<?> theClass, String flavour) {
				if (BusConductor.class == theClass) {
					return BEAN_BUS_SERVICE_NAME;
				}
				else {
					return null;
				}
			}
			
			@Override
			public BusConductor getService(String serviceName)
					throws IllegalArgumentException {
				if (busConductor == null) {
					throw new NullPointerException(
							"Bus Service Not Available until the Bus is Running.");
				}
				return busConductor;
			}
			
			@Override
			public String toString() {
				BusConductor busConductor = MegaBeanBus.this.busConductor;
				if (busConductor == null) {
					return "No Bus Service Until Running.";
				}
				else {
					return busConductor.toString();
				}
			}
		};
	}

	public BusConductor getBusConductor() {
		return busConductor;
	}

	public void setBusConductor(BusConductor busConductor) {
		this.busConductor = busConductor;
	}

	public boolean isNoAutoLink() {
		return noAutoLink;
	}

	public void setNoAutoLink(boolean noAutoLink) {
		this.noAutoLink = noAutoLink;
	}
	
}
