package org.oddjob.jobs;

import java.io.OutputStream;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanView;
import org.oddjob.arooa.reflect.BeanViews;
import org.oddjob.beanbus.BasicBeanBus;
import org.oddjob.beanbus.BeanSheet;
import org.oddjob.beanbus.BusException;

/**
 * @oddjob.description Create a simple listing of the properties of 
 * of beans.
 * 
 * @author rob
 *
 */
public class BeanReportJob implements Runnable, ArooaSessionAware {

//	private static final Logger logger = Logger.getLogger(BeanReportJob.class);
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The name of this job. 
	 * @oddjob.required No.
	 */
	private String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The beans to report on. 
	 * @oddjob.required Yes.
	 */
	private Iterable<? extends Object> beans;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The Output to where the report will 
	 * be written. 
	 * @oddjob.required Yes.
	 */
	private OutputStream output;

	/** 
	 * @oddjob.property
	 * @oddjob.description No Header is produced.
	 * @oddjob.required No.
	 */
	private boolean noHeaders;

	
	/** Required for bean access. */
	private ArooaSession session;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Specifies the order and titles of 
	 * the properties. 
	 * @oddjob.required No.
	 */
	private BeanView beanView;

	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public void run() {

		if (beans == null) {
			throw new NullPointerException("No beans.");
		}
		
		if (output == null) {
			throw new NullPointerException("No output.");
		}

		final BeanSheet sheet = new BeanSheet();
		sheet.setArooaSession(session);
		sheet.setOutput(output);
		sheet.setNoHeaders(noHeaders);
		sheet.setBeanViews(new BeanViews() {
			@Override
			public BeanView beanViewFor(ArooaClass arooaClass) {
				return beanView;
			}
		});
		
		BasicBeanBus<Iterable<? extends Object>> bus = 
			new BasicBeanBus<Iterable<? extends Object>>();
		bus.setTo(sheet);
		
		try {
			bus.startBus();
			bus.accept(beans);
			bus.stopBus();
		} catch (BusException e) {
			throw new RuntimeException(e);
		}
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Iterable<? extends Object> getBeans() {
		return beans;
	}

	public void setBeans(Iterable<? extends Object> beans) {
		this.beans = beans;
	}

	public OutputStream getOutput() {
		return output;
	}

	public void setOutput(OutputStream output) {
		this.output = output;
	}
	
	public boolean isNoHeaders() {
		return noHeaders;
	}

	public void setNoHeaders(boolean noHeader) {
		this.noHeaders = noHeader;
	}

	public BeanView getBeanView() {
		return beanView;
	}

	public void setBeanView(BeanView beanExtraProvider) {
		this.beanView = beanExtraProvider;
	}	
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		return name;
	}	
}
