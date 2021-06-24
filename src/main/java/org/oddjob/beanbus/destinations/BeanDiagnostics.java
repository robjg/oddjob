package org.oddjob.beanbus.destinations;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.io.StdoutType;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {@link BusFilter} that analyses beans.
 * 
 * 
 * @author rob
 *
 * @param <T> The type of the beans to be analysed.
 */
public class BeanDiagnostics<T> extends AbstractFilter<T, T> 
implements ArooaSessionAware, Runnable, Closeable {

	private final Set<ArooaClass> types = new LinkedHashSet<>();

	private PropertyAccessor accessor;
	
	private int nulls;

	private int count;
	
	private OutputStream output;
	
	public void setArooaSession(ArooaSession session) {
		this.accessor = session.getTools().getPropertyAccessor();
	}

	@Start
	public void run() {
		types.clear();
		nulls = 0;
		count = 0;

		if (output == null) {
			try {
				output = new StdoutType().toValue();
			} catch (ArooaConversionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Stop
	public void close() throws IOException {
		PrintStream out = new PrintStream(output);
		out.println("Analysed " + count + " beans. Discovered " +
				types.size() + " types.");
		for (ArooaClass type : types) {
			printTypeInfo(type, out);
		}
		out.close();
	}

	@Override
	protected T filter(T from) {
		
		++count;
		
		if (from == null) {
			++nulls;
		}
		else {
			ArooaClass type = accessor.getClassName(from);
			types.add(type);
		}
		return from;
	}
	
	public void setOutput(OutputStream output) {
		this.output = output;
	}
	
	public OutputStream getOutput() {
		return output;
	}
	
	public Set<ArooaClass> getTypes() {
		return types;
	}

	public int getCount() {
		return count;
	}
	
	public void printTypeInfo(ArooaClass type, PrintStream out) {
		
		out.println("Type: " + type);
		out.println(" Properties:");
		BeanOverview overview = type.getBeanOverview(accessor);
		String[] properties = overview.getProperties();
		Arrays.sort(properties);
		for (String property : properties) {
			out.println("  " + property + ": " + 
					overview.getPropertyType(property).getName() +
					(overview.isIndexed(property) ? ", indexed" : "") +
					(overview.isMapped(property) ? ", mapped" : "") +
					(overview.hasReadableProperty(property) ? "" : " (Write Only)") +
					(overview.hasWriteableProperty(property) ? "" : " (Read Only)")
				);
		}
	}
}
