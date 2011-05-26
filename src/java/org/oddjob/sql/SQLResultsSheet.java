package org.oddjob.sql;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.BeanSheet;
import org.oddjob.beanbus.BusAware;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusException;
import org.oddjob.beanbus.BusListener;
import org.oddjob.beanbus.CrashBusException;
import org.oddjob.beanbus.StageEvent;
import org.oddjob.beanbus.StageListener;
import org.oddjob.io.StdoutType;
import org.oddjob.util.StreamPrinter;

/**
 * @oddjob.description Writes SQL results to an output stream.
 * 
 * @oddjob.example
 * 
 * A result sheet for multiple statements.
 * 
 * {@oddjob.xml.resource org/oddjob/sql/SQLResultsSheetExample.xml}
 * 
 * This writes the following to the console:
 * 
 * <code><pre>
 * [0 rows affected, 16 ms.]
 * 
 * [1 rows affected, 0 ms.]
 * 
 * [1 rows affected, 0 ms.]
 * 
 * TYPE    VARIETY  COLOUR         SIZE
 * ------  -------  -------------  -----
 * Apple   Cox      Red and Green  7.6
 * Orange  Jaffa    Orange         9.245
 * 
 * [2 rows, 212 ms.]
 * 
 * [0 rows affected, 0 ms.]
 * 
 * </pre></code>
 * 
 * @author rob
 *
 */
public class SQLResultsSheet implements SQLResultsProcessor, 
		ArooaSessionAware, BusAware {
	
	private static final Logger logger = Logger.getLogger(SQLResultsSheet.class);
	
	/**
	 * @oddjob.property
	 * @oddjob.description The output stream to write results to.
	 * @oddjob.required No. Defaults to stdout.
	 */
	private OutputStream output;	
	
	/**
	 * @oddjob.property
	 * @oddjob.description Don't display headings.
	 * @oddjob.required No. Defaults to showing headings.
	 */
	private boolean dataOnly;
	
	/** The session. */
	private ArooaSession session;
	
	/** Used to display elapsed time. */
	private long elapsedTime = System.currentTimeMillis();
	
	@Override
	@ArooaHidden
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public void accept(Object bean) throws BadBeanException {
		
		elapsedTime = System.currentTimeMillis() - elapsedTime;
		
		if (output == null) {
			return;
		}
		
		if (bean instanceof List<?>) {
			List<?> iterable = (List<?>) bean;
			
			BeanSheet sheet = new BeanSheet();
			sheet.setOutput(new FilterOutputStream(output) {
				public void close() throws IOException {};{}
			});
			sheet.setArooaSession(session);
			sheet.setNoHeaders(dataOnly);
			
			sheet.accept(iterable);			
			
			if (!dataOnly) {
				new StreamPrinter(output).println();
				new StreamPrinter(output).println("[" + iterable.size() + " rows, " +
						elapsedTime + " ms.]");
			}
		}
		else if (bean instanceof UpdateCount) {
			
			if (!dataOnly) {
				UpdateCount updateCount = (UpdateCount) bean;
					
				new StreamPrinter(output).println("[" + updateCount.getCount() + " rows affected, " +
					elapsedTime + " ms.]");
			}
		}
		else {
			throw new BadBeanException(bean, "Unexpected bean type.");
		}		
	}

	public OutputStream getOutput() {
		return output;
	}

	public void setOutput(OutputStream output) {
		this.output = output;
	}
	
	public boolean isDataOnly() {
		return dataOnly;
	}

	public void setDataOnly(boolean dataOnly) {
		this.dataOnly = dataOnly;
	}

	@Override
	@ArooaHidden
	public void setBus(BeanBus bus) {
		
		final StageListener stageListener = new StageListener() {
			
			@Override
			public void stageStarting(StageEvent event) {
				elapsedTime = System.currentTimeMillis();
			}
			
			@Override
			public void stageComplete(StageEvent event) {
				if (!dataOnly) {
					new StreamPrinter(output).println();
				}
			}
		};
		
		bus.addBusListener(new BusListener() {
			
			@Override
			public void busStarting(BusEvent event) throws CrashBusException {
				if (output == null) {
					try {
						output = new StdoutType().toValue();
					} catch (ArooaConversionException e) {
						throw new CrashBusException(e);
					}
				}						
			}
			
			@Override
			public void busStopping(BusEvent event) throws CrashBusException {
			}
			
			@Override
			public void busCrashed(BusEvent event, BusException e) {
			}
			
			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
				event.getSource().removeStageListener(stageListener);
				try {
					if (output != null) {
						output.close();
					}
				}
				catch (IOException ioe) {
					logger.error("Failed to close output.", ioe);
				}
			}			
		});

		bus.addStageListener(stageListener);
	}	
}
