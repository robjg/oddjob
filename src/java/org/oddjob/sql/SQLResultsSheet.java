package org.oddjob.sql;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListenerAdapter;
import org.oddjob.beanbus.destinations.BeanSheet;
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
public class SQLResultsSheet extends BeanFactoryResultHandler
implements ArooaSessionAware {
	
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
	
	private final List<Object> beans = new ArrayList<Object>();
	
	@Override
	@ArooaHidden
	public void setArooaSession(ArooaSession session) {
		super.setArooaSession(session);
		this.session = session;
	}
	
	@Override
	protected void accept(Object bean) {
		
		beans.add(bean);
	}

	public void writeBeans(List<Object> beans) {
		
		elapsedTime = System.currentTimeMillis() - elapsedTime;
		
		if (beans.size() > 0 && 
				beans.get(0) instanceof UpdateCount &&
				!dataOnly) {
			
			UpdateCount updateCount = (UpdateCount) beans.get(0);
				
			new StreamPrinter(output).println("[" + updateCount.getCount() + " rows affected, " +
				elapsedTime + " ms.]");
		}
		else {
		
			BeanSheet sheet = new BeanSheet();
			sheet.setArooaSession(session);
			sheet.setNoHeaders(dataOnly);
			sheet.setOutput(output);
		
			sheet.writeBeans(beans);
		
			if (!dataOnly) {
				new StreamPrinter(output).println();
				new StreamPrinter(output).println("[" + beans.size() + " rows, " +
						elapsedTime + " ms.]");
			}
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

	@ArooaHidden
	@Inject
	public void setBusConductor(BusConductor busConductor) {
		super.setBusConductor(busConductor);
		
		busConductor.addBusListener(new BusListenerAdapter() {
			
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
				writeBeans(beans);
				beans.clear();
				
				if (!dataOnly) {
					new StreamPrinter(output).println();
				}
			}

			@Override
			public void busTerminated(BusEvent event) {
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
