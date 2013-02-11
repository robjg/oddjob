package org.oddjob.sql;

import java.util.Collection;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.registry.Services;
import org.oddjob.beanbus.BasicBeanBus;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.BeanBusCommand;
import org.oddjob.beanbus.BeanBusService;
import org.oddjob.beanbus.BusAware;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListener;
import org.oddjob.beanbus.BusListenerAdapter;

/**
 * @oddjob.description A {@link SQLResultHandler} that attaches to 
 * {@link BeanBus} components.
 * 
 * @author rob
 */
public class SQLResultsBeanBus extends BeanFactoryResultHandler
implements BusAware, BusConductor, BeanBusService {
	
	private BusConductor incomingBusConductor;
	
	private final BasicBeanBus<Object> beanBus = new BasicBeanBus<Object>(
			new BeanBusCommand() {
				@Override
				public void run() throws BusCrashException {
					incomingBusConductor.requestBusStop();
				}
			});

	@ArooaHidden
	@Override
	public void setBeanBus(BusConductor busConductor) {
		this.incomingBusConductor = busConductor;
		busConductor.addBusListener(new BusListenerAdapter() {
			@Override
			public void busStarting(BusEvent event) throws BusCrashException {
				beanBus.startBus();
			}
			@Override
			public void tripEnding(BusEvent event) throws BusCrashException {
				beanBus.cleanBus();
			}
			@Override
			public void busStopping(BusEvent event) throws BusCrashException {
				beanBus.stopBus();
			}
			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
			}
		});
	}
	
	@Override
	protected void accept(Object bean) {
		
		try {
			beanBus.accept(bean);
		} catch (BusCrashException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void addBusListener(BusListener listener) {
		beanBus.addBusListener(listener);
	}
	
	@Override
	public void removeBusListener(BusListener listener) {
		beanBus.removeBusListener(listener);
	}
	
	@Override
	public void requestBusStop() throws BusCrashException {
		beanBus.requestBusStop();
	}
	
	@Override
	public void cleanBus() throws BusCrashException {
		beanBus.cleanBus();
	}
	
	@Override
	public BusConductor getService(String serviceName)
			throws IllegalArgumentException {
		if (BEAN_BUS_SERVICE_NAME.equals(serviceName)) {
			return beanBus;
		}
		else {
			return null;
		}
	}
	
	@Override
	public String serviceNameFor(Class<?> theClass, String flavour) {
		if (BeanBusService.class == theClass) {
			return BEAN_BUS_SERVICE_NAME;
		}
		else {
			return null;
		}
	}
	
	@Override
	public Services getServices() {
		return this;
	}
		
	public void setTo(Collection<? super Object> to) {
		beanBus.setTo(to);
	}
	
	public Collection<? super Object> getTo() {
		return beanBus.getTo();
	}
}
