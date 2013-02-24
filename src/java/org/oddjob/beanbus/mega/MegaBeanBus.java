package org.oddjob.beanbus.mega;


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
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusListener;
import org.oddjob.beanbus.BusService;
import org.oddjob.beanbus.BusServiceProvider;
import org.oddjob.framework.StructuralJob;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.StateOperator;

/**
 *
 * @oddjob.description 
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
	
	private BusConductor busConductor;
	
	private final BusConductor delgatingBusConductor = new BusConductor() {
		
		@Override
		public void requestBusStop() {
			if (busConductor == null) {
				throw new NullPointerException("Bus Conductor unset.");
			}
			busConductor.requestBusStop();
		}
		
		@Override
		public void removeBusListener(BusListener listener) {
			if (busConductor == null) {
				throw new NullPointerException("Bus Conductor unset.");
			}
			busConductor.removeBusListener(listener);
		}
		
		@Override
		public void cleanBus() throws BusCrashException {
			if (busConductor == null) {
				throw new NullPointerException("Bus Conductor unset.");
			}
			busConductor.cleanBus();
		}
		
		@Override
		public void addBusListener(BusListener listener) {
			if (busConductor == null) {
				throw new NullPointerException("Bus Conductor unset.");
			}
			busConductor.addBusListener(listener);
		}
	};
		
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
	@Override
	protected void execute() throws Exception {
		
		Object[] children = childHelper.getChildren();

		for (Object child : children) {
			
			if (child instanceof BusServiceProvider) {
				busConductor = ((BusServiceProvider) child).getServices(
						).getService(BusService.BEAN_BUS_SERVICE_NAME);
			}
			
			if (child instanceof BusPart) {
				((BusPart) child).prepare();
			}
		}
		
		for (Object child : children) {
			
			if (child instanceof Runnable) {
				((Runnable) child).run();
			}
		}
		
	}	

	@Override
	public BusService getServices() {
		return new BusService(delgatingBusConductor);
	}
	
}
