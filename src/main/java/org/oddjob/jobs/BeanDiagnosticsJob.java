package org.oddjob.jobs;

import java.io.OutputStream;
import java.util.Arrays;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.beanbus.SimpleBusService;
import org.oddjob.beanbus.destinations.BeanDiagnostics;
import org.oddjob.beanbus.drivers.IterableBusDriver;
import org.oddjob.io.StdoutType;

/**
 * @oddjob.description Dump out the types and properites of a bean or 
 * beans.
 * 
 * @author rob
 *
 */
public class BeanDiagnosticsJob implements Runnable, ArooaSessionAware {

//	private static final Logger logger = LoggerFactory.getLogger(BeanReportJob.class);
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The name of this job. 
	 * @oddjob.required No.
	 */
	private String name;
	
	/**
	 * @oddjob.property
	 * @oddjob.description A single bean to analyse. 
	 * @oddjob.required Either this or the beans are required.
	 */
	private Object bean;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The beans to analyse. 
	 * @oddjob.required Either this or a bean is required.
	 */
	private Iterable<? extends Object> beans;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The Output to where the report will 
	 * be written. 
	 * @oddjob.required Yes.
	 */
	private OutputStream output;

	
	/** Required for bean access. */
	private ArooaSession session;
	

	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public void run() {

		if (bean != null) {
			beans = Arrays.asList(bean);
		}
		
		if (beans == null) {
			throw new NullPointerException("No beans.");
		}
		
		if (output == null) {
			try {
				output = new StdoutType().toValue();
			} catch (ArooaConversionException e) {
				throw new RuntimeException(e);
			}
		}

		IterableBusDriver<Object> iterableBusDriver = 
				new IterableBusDriver<Object>();
	
		BeanDiagnostics<Object> diagnostics = new BeanDiagnostics<Object>();
		diagnostics.setArooaSession(session);
		diagnostics.setOutput(output);
		diagnostics.setBusConductor(iterableBusDriver.getServices().getService(
				SimpleBusService.BEAN_BUS_SERVICE_NAME));
		
		iterableBusDriver.setBeans(beans);
		iterableBusDriver.setTo(diagnostics);
		
		iterableBusDriver.run();
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
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
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		return name;
	}

}
