package org.oddjob.beanbus.mega;


import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.deploy.annotations.ArooaInterceptor;
import org.oddjob.arooa.design.SerializableGenericDesignFactory;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeEvent;
import org.oddjob.arooa.runtime.RuntimeListenerAdapter;
import org.oddjob.beanbus.*;
import org.oddjob.beanbus.adapt.OutboundStrategies;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.StateOperator;

import javax.inject.Inject;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

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
	private transient volatile ConfigurationOwnerSupport configurationOwnerSupport;
	
	private transient volatile BusConductor busConductor;

	private transient volatile Executor executor;

	private volatile boolean noAutoLink;
	
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
	public SerializableDesignFactory rootDesignFactory() {
		return new SerializableGenericDesignFactory(
				this.getClass());
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

		StatefulBusSupervisor adaptor = null;
		
		Object previousChild = null;

		for (Object child: children) {

			if (!noAutoLink && previousChild != null &&
					child instanceof Consumer) {

				final Outbound outbound = new OutboundStrategies()
						.outboundFor(previousChild, getArooaSession());

				if (outbound != null) {

					RuntimeConfiguration previousRuntime =
					getArooaSession().getComponentPool().contextFor(previousChild)
							.getRuntime();

					final Object finalPreviousChild = previousChild;

					previousRuntime.addRuntimeListener(new RuntimeListenerAdapter() {
						@Override
						public void afterConfigure(RuntimeEvent event) throws ArooaException {
							outbound.setTo((Consumer) child);

							logger().info("Automatically Linked Outbound [" +
									finalPreviousChild + "] to [" + child + "]");

							previousRuntime.removeRuntimeListener(this);
						}
					});
				}
			}

			previousChild = child;
		}

		try {
			final SimpleBusConductor busConductor = new SimpleBusConductor(children);

			BusControls busControls = new BusControls() {
				@Override
				public void stopBus() {
					busConductor.close();
				}

				@Override
				public void crashBus(Throwable exception) {
					busConductor.actOnBusCrash(exception);
				}
			};


			new StatefulBusSupervisor(busControls, executor)
					.supervise(children);

			this.busConductor = busConductor;

			busConductor.run();
		}
		finally {
			busConductor = null;
		}		
	}	

	static void flushBus(Iterable<Object> children) {
		for (Object child: children) {
			if (child instanceof Flushable) {
				try {
					((Flushable) child).flush();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
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
				if (theClass.isAssignableFrom(BusConductor.class)) {
					return BEAN_BUS_SERVICE_NAME;
				}
				else {
					return null;
				}
			}
			
			@Override
			public BusConductor getService(String serviceName)
					throws IllegalArgumentException {
				
				BusConductor busConductor = MegaBeanBus.this.busConductor;
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

	public Executor getExecutor() {
		return this.executor;
	}

	@Inject
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
}
