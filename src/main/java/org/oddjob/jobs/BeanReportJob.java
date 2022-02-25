package org.oddjob.jobs;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.BeanView;
import org.oddjob.beanbus.SimpleBusConductor;
import org.oddjob.beanbus.destinations.BeanSheet;
import org.oddjob.beanbus.drivers.IterableBusDriver;
import org.oddjob.io.StdoutType;

import java.io.OutputStream;
import java.util.Objects;

/**
 * @oddjob.description Create a simple listing of the properties of 
 * of beans.
 * 
 * @author rob
 *
 */
public class BeanReportJob implements Runnable, ArooaSessionAware {

//	private static final Logger logger = LoggerFactory.getLogger(BeanReportJob.class);
	
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
	private Iterable<?> beans;
	
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

		Iterable<?> beans = Objects.requireNonNull(this.beans, "No beans.");
		
		if (output == null) {
			try {
				output = new StdoutType().toOutputStream();
			} catch (ArooaConversionException e) {
				throw new RuntimeException(e);
			}
		}

		IterableBusDriver<Object> iterableBusDriver =
				new IterableBusDriver<>();
	
		final BeanSheet sheet = new BeanSheet();
		sheet.setArooaSession(session);
		sheet.setOutput(output);
		sheet.setNoHeaders(noHeaders);
		sheet.setBeanViews(arooaClass -> beanView);

		iterableBusDriver.setBeans(beans);
		iterableBusDriver.setTo(sheet);

		SimpleBusConductor busConductor = new SimpleBusConductor(iterableBusDriver, sheet);
		busConductor.run();
		busConductor.close();
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Iterable<?> getBeans() {
		return beans;
	}

	public void setBeans(Iterable<?> beans) {
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
