package org.oddjob.beanbus.bus;


import org.oddjob.FailedToStopException;
import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaInterceptor;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeEvent;
import org.oddjob.arooa.runtime.RuntimeListenerAdapter;
import org.oddjob.beanbus.*;
import org.oddjob.beanbus.adapt.OutboundStrategies;
import org.oddjob.beanbus.mega.BusControls;
import org.oddjob.beanbus.mega.StatefulBusSupervisor;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateOperator;
import org.oddjob.util.Restore;

import javax.inject.Inject;
import java.io.Flushable;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 *
 * @oddjob.description A job that allows the construction of a 
 * {@link org.oddjob.beanbus.BeanBus}.
 * <p>
 * A Bean Bus is an assembly of {@link Consumer}s.
 * 
 * 
 *
 * 
 * @author Rob Gordon
 * 
 */
@ArooaInterceptor("org.oddjob.beanbus.bus.BeanBusInterceptor")
public class BeanBusJob extends StructuralJob<Object>
implements BusServiceProvider, Consumer<Object> {

    private static final long serialVersionUID = 2012021500L;

	private transient volatile BusConductor busConductor;

	private transient volatile Executor executor;

	private volatile boolean noAutoLink;

	private Consumer<Object> to;

	private Consumer<Object> first;

	/**
	 * Only constructor.
	 */
	public BeanBusJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
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
	public void setOf(int index, Object child) {
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
	@SuppressWarnings({ "unchecked"})
	@Override
	protected void execute() throws Exception {
		
		Object[] children = childHelper.getChildren();

		StatefulBusSupervisor adaptor = null;
		
		Object previousChild = null;

		for (Object child: children) {

			if (this.first == null && child instanceof Consumer) {
				this.first = (Consumer<Object>) child;
			}

			if (!noAutoLink && previousChild != null &&
					child instanceof Consumer) {

				maybeSetConsumerOnOutbound(previousChild, (Consumer<?>) child);
			}

			previousChild = child;
		}

		if (to != null && previousChild != null ) {
			maybeSetConsumerOnOutbound(previousChild, to);
		}

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

	protected void maybeSetConsumerOnOutbound(Object maybeOutbound, Consumer<?> consumer) {

		final Outbound<?> outbound = new OutboundStrategies()
				.outboundFor(maybeOutbound, getArooaSession());

		if (outbound != null) {

			RuntimeConfiguration previousRuntime =
					getArooaSession().getComponentPool().contextFor(maybeOutbound)
							.getRuntime();

			previousRuntime.addRuntimeListener(new RuntimeListenerAdapter() {
				@SuppressWarnings("unchecked")
				@Override
				public void afterConfigure(RuntimeEvent event) throws ArooaException {
					outbound.setTo((Consumer<Object>) consumer);

					logger().info("Automatically Linked Outbound [" +
							maybeOutbound + "] to [" + consumer + "]");

					previousRuntime.removeRuntimeListener(this);
				}
			});
		}
	}

	@Override
	public void accept(Object bean) {

		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {

			if (StateConditions.STARTED.test(stateHandler().getState())) {
				first.accept(bean);
			}
			else {
				logger().warn("Ignoring because service not started: {}", bean);
			}
		}
		catch (Exception ex) {
			logger().error("Exception processing bean: {}", bean, ex);
			stateHandler().runLocked(() -> getStateChanger().setStateException(ex));
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
		this.busConductor = null;
	}

	@Override
	protected void onStop() throws FailedToStopException {
		this.busConductor.close();
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
				
				BusConductor busConductor = BeanBusJob.this.busConductor;
				if (busConductor == null) {
					throw new NullPointerException(
							"Bus Service Not Available until the Bus is Running.");
				}
				return busConductor;
			}
			
			@Override
			public String toString() {
				BusConductor busConductor = BeanBusJob.this.busConductor;
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

	public Consumer<Object> getTo() {
		return to;
	}

	@Destination
	public void setTo(Consumer<Object> to) {
		this.to = to;
	}
}
