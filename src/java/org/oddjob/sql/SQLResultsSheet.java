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
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BeanSheet;
import org.oddjob.beanbus.BusAware;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusException;
import org.oddjob.beanbus.BusListener;
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
public class SQLResultsSheet extends AbstractDestination<Object>
implements ArooaSessionAware, BusAware {
	
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
	public boolean add(Object bean) {
		
		elapsedTime = System.currentTimeMillis() - elapsedTime;
		
		if (output == null) {
			return false;
		}
		
		if (bean instanceof List<?>) {
			List<?> iterable = (List<?>) bean;
			
			BeanSheet sheet = new BeanSheet();
			sheet.setOutput(new FilterOutputStream(output) {
				public void close() throws IOException {};{}
			});
			sheet.setArooaSession(session);
			sheet.setNoHeaders(dataOnly);
			
			sheet.add(iterable);			
			
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
			throw new IllegalArgumentException("Unexpected bean type.");
		}		
		
		return true;
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
	public void setBeanBus(BusConductor bus) {
		
		
		bus.addBusListener(new BusListener() {
			
			@Override
			public void busStarting(BusEvent event) throws BusCrashException {
				if (output == null) {
					try {
						output = new StdoutType().toValue();
					} catch (ArooaConversionException e) {
						throw new BusCrashException(e);
					}
				}						
			}
			
			@Override
			public void tripBeginning(BusEvent event) {
				elapsedTime = System.currentTimeMillis();
			}
			
			@Override
			public void tripEnding(BusEvent event) {
				if (!dataOnly) {
					new StreamPrinter(output).println();
				}
			}
			
			@Override
			public void busStopping(BusEvent event) throws BusCrashException {
			}
			
			@Override
			public void busCrashed(BusEvent event, BusException e) {
			}
			
			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
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

	}	
}
