package org.oddjob.beanbus.destinations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.BeanView;
import org.oddjob.arooa.reflect.BeanViews;
import org.oddjob.arooa.reflect.FallbackBeanView;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.TrackingBusListener;

/**
 * @oddjob.description Create a simple database style report from a list
 * of beans.
 * 
 * @author rob
 *
 */
public class BeanSheet extends AbstractDestination<Object>
implements ArooaSessionAware {

	private static final Logger logger = Logger.getLogger(BeanSheet.class);
	
	/** The padding character. A space. */
	private static final char PADDING = ' ';

	/** The space between columns. */
	private static final String COLUMN_SPACE = 
		pad(PADDING, 2);

	/** The underline character. */
	private static final char UNDERLINE = '-';
	
	private OutputStream output;

	private boolean noHeaders;
	
	private PropertyAccessor accessor;
	
	private ArooaConverter converter;
	
	private BeanViews beanViews;
	
	private final List<Object> beans = new ArrayList<Object>();

	private String name;
	
	private final TrackingBusListener busListener = 
			new TrackingBusListener() {
		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			if (output == null) {
				throw new BusCrashException("No output.");
			}						
		}
					
		@Override
		public void tripEnding(BusEvent event) throws BusCrashException {
			writeBeans(beans);
			beans.clear();
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
	};
	
	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		this.accessor = 
			session.getTools().getPropertyAccessor();
		this.converter =
			session.getTools().getArooaConverter();
	}

	@Override
	public boolean add(Object bean) {
		
		beans.add(bean);
		return true;
	}

	public void writeBeans(Iterable<?> beans) {
		
		if (beans == null) {
			throw new NullPointerException("No beans.");
		}
		
		PrintStream out = new PrintStream(output);
		
		List<Row> rows = new ArrayList<Row>();
		
		Header header = null;
				
		int count = 0;
		for (Object bean : beans) {
						
			if (header == null) {
				header = new Header(bean);
				rows.add(header);
			}
			
			Line line = header.process(bean);
			rows.add(line);
			++count;
		}		

		logger.info("Reporting on " + count + " beans");
		
		for (Row row : rows) {
			row.printTo(out);
		}		

		out.flush();
	}
	
	@ArooaHidden
	@Inject
	public void setBeanBus(BusConductor busConductor) {
		busListener.setBusConductor(busConductor);
	}
	
	interface Row {

		void printTo(PrintStream out);
	}
	
	class Line implements Row {

		String[] values;
		
		Header header;
		
		Line(String[] values, Header header) {
			this.values = values;
			this.header = header;
		}
		
		@Override
		public void printTo(PrintStream out) {
			for (int i = 0; i < values.length; ++i) {
				if (i != 0) {
					out.print(COLUMN_SPACE);
				}
				out.print(values[i]);
				if (i != values.length - 1) {
					out.print(pad(PADDING, 
							header.widths[i] - values[i].length()));
				}
			}
			out.println();
		}
	}
		
	class Header implements Row {
		
		private final String[] properties;
		
		private final String[] headings;
		
		private final int[] widths;
				
		Header(Object bean) {
			
			ArooaClass arooaClass = accessor.getClassName(bean);
			
			BeanView view = null;
			if (beanViews != null) {
				view = beanViews.beanViewFor(arooaClass);
			}
			if (view == null) {
				view = new FallbackBeanView(accessor, bean);
			}
			
			String[] allProperties = view.getProperties();
			
			List<String> readables = new ArrayList<String>();
			
			BeanOverview overview = arooaClass.getBeanOverview(accessor);
			
			for (String property : allProperties) {

				if ("class".equals(property)) {
					continue;
				}
				if (overview.hasReadableProperty(property)) {
					readables.add(property);
				}
			}

			this.properties = readables.toArray(
					new String[readables.size()]);
			this.headings = new String[properties.length];
			this.widths = new int[properties.length];
			
			for (int i = 0; i < properties.length; ++i) {
				headings[i] = view.titleFor(properties[i]);
				widths[i] = headings[i].length();
			}
			logger.info("Headings: " + Arrays.toString(headings));
		}

		@Override
		public void printTo(PrintStream out) {
			if (noHeaders) {
				return;
			}
			
			for (int i = 0; i < headings.length; ++i) {
				if (i != 0) {
					out.print(COLUMN_SPACE);
				}
				out.print(headings[i]);
				if (i != headings.length - 1)
				out.print(pad(PADDING, 
						widths[i] - headings[i].length()));
			}
			out.println();
			for (int i = 0; i < headings.length; ++i) {
				if (i != 0) {
					out.print(COLUMN_SPACE);
				}
				out.print(pad(UNDERLINE, 
						widths[i]));
			}
			out.println();
		}
		
		Line process(Object bean) {
			String[] values = new String[properties.length];
			
			for (int i = 0; i < properties.length; ++i) {
				String property = properties[i];
		
				Object value = accessor.getProperty(bean, property);

				String string = null;
				try {
					string = converter.convert(value, String.class);
				}
				catch (ArooaConversionException e) {
					string = pad('#', widths[i]);
				}
				if (string == null) {
					string = "";
				}
				if (widths[i] < string.length()) {
					widths[i] = string.length();
				}
				
				values[i] = string;
			}
			
			return new Line(values, this);
		}
	}
	
	@Override
	public boolean isEmpty() {
		return beans.size() == 0;
	}
	
	public int getBeanCount() {
		return beans.size();
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

	public void setNoHeaders(boolean noHeaders) {
		this.noHeaders = noHeaders;
	}

	public BeanViews getBeanViews() {
		return beanViews;
	}

	public void setBeanViews(BeanViews beanViews) {
		this.beanViews = beanViews;
	}	
	
	private static String pad(char padding, int size) {
		if (size < 1) {
			return new String("");
		}
		char[] buff = new char[size];
		Arrays.fill(buff, padding);
		return new String(buff);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {

		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}
}
