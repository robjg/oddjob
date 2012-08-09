package org.oddjob.script;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;

/**
 * An {@link Invoker} for a java method.
 * 
 * @author rob
 *
 */
public class MethodInvoker implements Invoker {

	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
			registry.register(Object.class, Invoker.class, 
					new Convertlet<Object, Invoker>() {
				public Invoker convert(Object from) {
					return new MethodInvoker(from);				
				}
			});
		}
	}
	
	private final Object target;
	
	/**
	 * Constructor.
	 * 
	 * @param target The object that contains the method.
	 */
	public MethodInvoker(Object target) {
		if (target == null) {
			throw new NullPointerException("No target for invoker");
		}
		this.target = target;
	}
		
	@Override
	public Object invoke(String name, InvokerArguments parameters) {
		Class<?> cl;
		
		MethodName methodName = new MethodName(name); 
		
		Object object = this.target;
		
		if (methodName.staticMethod && object instanceof Class<?>) {
			cl = (Class<?>) object;
			object = null;
		}
		else {
			cl = object.getClass();
		}
		
		
		Method[] ms = cl.getMethods();
		
		Method found = null;
		Object[] args = null;
		
		for (Method m: ms) {
			if (!m.getName().equals(methodName.method)) {
				continue;
			}
		
			if (parameters.size() != m.getParameterTypes().length) {
				continue;
			}
			
			args = new Object[parameters.size()];
			try {
				for (int i = 0; i < args.length; ++i) {
					args[i] = parameters.getArgument(i, 
							m.getParameterTypes()[i]);
				}
			}
			catch (ArooaConversionException e) {
				continue;
			}
			
			found = m;
			break;
		}
		
		if (found == null) {
			throw new IllegalArgumentException("No method found on [" +
					target + "] " + name);
		}
		
		try {
			return found.invoke(object, args);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed invoking " + 
					name, e);
		}
	}
	
	class MethodName {
		
		final String method;
		final boolean staticMethod;
		
		public MethodName(String name) {
			Pattern pattern = Pattern.compile(
					"(static\\s+)?(\\w+)");
			//       1            2
			
			Matcher matcher = pattern.matcher(name);
			
			if (!matcher.matches()) {
				throw new IllegalArgumentException(
						"Failed to parse method name " +
						name);
			}
			
			staticMethod = matcher.group(1) != null;
			method = matcher.group(2);
		}
	}
}
