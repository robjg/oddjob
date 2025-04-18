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
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateOperator;
import org.oddjob.util.Restore;

import java.io.Flushable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *
 * @oddjob.description Links components in a data pipeline. Components
 * provide data by accepting an {@link Consumer} either by
 * being an {@link Outbound} or by marking a setter with the {@link Destination}
 * annotation. Components accept data by being a {@link Consumer}. Components
 * can be both.
 * <p>
 * This component parent provides the following features over other component parents
 * such as {@link org.oddjob.jobs.structural.SequentialJob}:
 * <ul>
 *     <li>Components will be automatically linked to the next component
 *     unless this is disabled.</li>
 *     <li>Any plain {@link Consumer} will appear in the bus as a service with
 *     appropriate icons and state.</li>
 *     <li>Components will be run (or started) in reverse order so destinations
 *     are ready to receive data before it is sent by the previous components.</li>
 *     <li>Components will be stopped in order so components that send data are
 *     stopped before the destinations that receive the data.</li>
 *     <li>If a component has a property setter of type {@link AutoCloseable} then
 *     one will be set automatically allowing the component to stop the bus.</li>
 *     <li>If a component has a property setter of type {@link Flushable} then
 *     one will be set automatically allowing the component to flush the bus.</li>
 *     <li>Any component that is {@link Flushable} will be flushed when a
 *     component flushes the bus. Flush will be called in component order.
 *     Flush will always be called when the bus stops, unless it crashes.</li>
 *     <li>If a component wishes to both stop and flush the bus, and doesn't mind
 *     a dependency on this framework it can provide a property setter of
 *     type {@link BusConductor} and one will be set automatically</li>
 *     <li>If a component enters an Exception state the bus will crash.
 *     Other components will be stopped in order.</li>
 * </ul>
 * </p>
 *
 * @oddjob.example
 *
 * A simple bus of 3 components. The first component is the bus driver that sends 3 beans down the pipe.
 * The second component is a function that doubles the price and the last component collects the
 * results.
 * <p>
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanTransformerExample.xml}
 *
 * @oddjob.example
 *
 * Shows how a bus can be nested to create side branches. The data is passed to each branch in turn.
 * <p>
 * {@oddjob.xml.resource org/oddjob/beanbus/bus/FilterExample.xml}
 *
 * @author Rob Gordon
 * 
 */
@ArooaInterceptor("org.oddjob.beanbus.bus.BeanBusInterceptor")
public class BasicBusService extends StructuralJob<Object>
implements ConductorServiceProvider, Consumer<Object>, Flushable {

    private static final long serialVersionUID = 2012021500L;

	/**
	 * @oddjob.property
	 * @oddjob.description Provides coordination facilities to the components of a bus. Set automatically
	 * and exposed for advance use only.
	 * @oddjob.required No.
	 */
	private transient volatile BusConductor busConductor;

	/**
	 * @oddjob.property
	 * @oddjob.description Bus components will automatically be linked unless this is true.
	 * @oddjob.required No, defaults to false.
	 */
	private volatile boolean noAutoLink;

	/**
	 * @oddjob.property
	 * @oddjob.description An onward consumer so that bus services may be nested.
	 * @oddjob.required No.
	 */
	private Consumer<Object> to;

	private Consumer<Object> first;

	private final AtomicInteger count = new AtomicInteger();

	/**
	 * Only constructor.
	 */
	public BasicBusService() {
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
	 * @oddjob.property of
	 * @oddjob.description The components of a Bus.
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

		new StatefulBusSupervisor(busControls)
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

			if (StateConditions.LIVE.test(stateHandler().getState())) {
				count.incrementAndGet();
				first.accept(bean);
				if (to != null) {
					to.accept(bean);
				}
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
		this.count.set(0);
		this.first = null;
	}

	@Override
	protected void onStop() throws FailedToStopException {
		this.busConductor.close();
	}

	@Override
	public void flush() {
		this.busConductor.flush();
	}

	/**
	 * @oddjob.property services
	 * @oddjob.description Provides services to other components of a bus. Exposed for advance use only.
	 * @oddjob.required Read Only.
	 */
	@Override
	public ConductorService getServices() {
		return new ConductorService() {
			
			@Override
			public String serviceNameFor(Class<?> theClass, String flavour) {
				if (theClass.isAssignableFrom(BusConductor.class)) {
					return CONDUCTOR_SERVICE_NAME;
				}
				else {
					return null;
				}
			}
			
			@Override
			public BusConductor getService(String serviceName)
					throws IllegalArgumentException {
				
				BusConductor busConductor = BasicBusService.this.busConductor;
				if (busConductor == null) {
					throw new NullPointerException(
							"Bus Service Not Available until the Bus is Running.");
				}
				return busConductor;
			}
			
			@Override
			public String toString() {
				BusConductor busConductor = BasicBusService.this.busConductor;
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

	public Consumer<Object> getTo() {
		return to;
	}

	@Destination
	public void setTo(Consumer<Object> to) {
		this.to = to;
	}

	public int getCount() {
		return count.get();
	}
}
