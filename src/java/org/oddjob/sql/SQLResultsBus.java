package org.oddjob.sql;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.TrackingBusListener;
import org.oddjob.beanbus.mega.MegaBeanBus;

/**
 * @oddjob.description 
 * 
 * A {@link SQLResultHandler} that attaches to 
 * {@link BeanBus} components.
 * 
 * @oddjob.example 
 * 
 * Writing to a list.
 * 
 * {@oddjob.xml.resource org/oddjob/sql/SQLResultsBusExample.xml}
 * 
 * @oddjob.example 
 * 
 * Within a {@link MegaBeanBus}.
 * 
 * {@oddjob.xml.resource org/oddjob/sql/SQLResultsBusExample2.xml}
 * 
 * @author rob
 */
public class SQLResultsBus extends BeanFactoryResultHandler {
	
	private static final Logger logger = Logger.getLogger(SQLResultsBus.class);
	
	private volatile Collection<? super Object> to;
	
	private int count = 0;
	
	private final TrackingBusListener busListener = 
			new TrackingBusListener() {
		
		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			
			if (to == null) {
				logger.info("No To Destination. Beans will be ignored.");
			}
		}
		
		@Override
		public void busStopping(BusEvent event) throws BusCrashException {
			logger.info("Sent " + count + " beans to destination [" + 
					to + "]");
		}
	};
	
	@Override
	protected void accept(Object bean) {
				
		if (to != null) {
			to.add(bean);
			++count;
		}
	}
	
	public void setTo(Collection<? super Object> to) {
		this.to = to;
	}
	
	public Collection<? super Object> getTo() {
		return to;
	}
	
	
	@ArooaHidden
	@Inject
	public void setBusConductor(BusConductor busConductor) {
		super.setBusConductor(busConductor);
		
		busListener.setBusConductor(busConductor);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " to [" + to +
				"], count=" + count;
	}
}
