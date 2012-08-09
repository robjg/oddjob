package org.oddjob.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionLookup;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.ConversionStep;
import org.oddjob.arooa.convert.Joker;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;

/**
 * @oddjob.description Invoke a java method or script snippet.
 * <p>
 * For a script, the source must be a <code>javax.script.Invocable</code>
 * object.
 * <p>
 * For a java method, the source specifies the object on which to invoke the
 * method. If the source is a class then the static method of the class is 
 * invoked.
 * 
 * @oddjob.example
 * 
 * Invoke a method on a bean. The method takes a single date parameter which
 * is uses to generate a time of day dependent greeting.
 * 
 * {@oddjob.xml.resource org/oddjob/script/InvokeMethod.xml}
 * 
 * The ${date} reference is there so that it can be injected
 * during a test, to get a guaranteed result. When this is example
 * is run as is, this is null so the system clock to be used
 * there by giving a real time based greeting.
 * <p>
 * One subtle point to note about Oddjob configuration that this example
 * highlights is to do with when types are resolved. 
 * The {@code invoke} type will be resolved when the 
 * {@code echo} job is run. The {@code schedule} type will be resolved when the 
 * {@code variables} job is
 * run. If the {@code echo} job were scheduled to run several hours after 
 * the {@code variables} job had run it would not give the correct greeting!
 * 
 * @oddjob.example
 * 
 * Invoke a static method of a class.
 * 
 * {@oddjob.xml.resource org/oddjob/script/InvokeStatic.xml}
 * 
 * @oddjob.example
 * 
 * Invoking a function of a script.
 * 
 * See the {@link ScriptJob} examples.
 * 
 * @author Rob.
 */
public class InvokeType 
implements ArooaValue, ArooaSessionAware {

	private static final Logger logger = Logger.getLogger(InvokeType.class);
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The java object or script Invocable on
	 * which to invoke the method/function.
	 * @oddjob.required Yes.
	 */
	private Invoker source;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The function/method to call. 
	 * @oddjob.required Yes.
	 */
	private String function;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The values to use as arguments. 
	 * @oddjob.required Must match the expected arguments..
	 */
	private List<ArooaValue> parameters = new ArrayList<ArooaValue>();

	/** Used to convert parameters. */
	private ArooaConverter converter;
	
	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
			registry.registerJoker(InvokeType.class,
					new Joker<InvokeType>() {
				public <T> ConversionStep<InvokeType, T> lastStep(
								Class<? extends InvokeType> from, 
								final Class<T> to, 
								ConversionLookup conversions) {
					
					return new ConversionStep<InvokeType, T>() {
						
						public Class<InvokeType> getFromClass() {
							return InvokeType.class;
						}
						
						public Class<T> getToClass() {
							return to;
						}
						
						public T convert(InvokeType from,
								ArooaConverter converter)
								throws ArooaConversionException {
							return converter.convert(from.toValue(), to);
						}
					};
				}
			});
		}
	}
	
	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		converter = session.getTools().getArooaConverter();
	}
	
	public Object toValue() throws ArooaConversionException {
		
		if (source == null) {
			throw new ArooaConversionException("No source.");
		}
	
		Object[] paramArray = parameters.toArray();
		
		logger.info("Invoking " + function + " with args " + 
					Arrays.toString(paramArray));
		
		Object result = source.invoke(function, 
				new ConvertableArguments(converter, paramArray));
		
		logger.info("Invocation of " + function + " complete, result " + 
				result);
		
		return result;
	}
	
	public Invoker getSource() {
		return source;
	}

	public void setSource(Invoker source) {
		this.source = source;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public ArooaValue getParameters(int index) {
		return parameters.get(index);
	}

	public void setParameters(int index, ArooaValue parameter) {
		if (parameter == null) {
			parameters.remove(index);
		}
		else {
			parameters.add(index, parameter);
		}
	}
	
	@Override
	public String toString() {
		return "Invoke: " + function;
	}
}
