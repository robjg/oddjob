package org.oddjob.script;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;

/**
 * An {@link Invoker} for a script snippet.
 * 
 * @author rob
 *
 */
public class ScriptInvoker implements Invoker {

	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
			registry.register(Invocable.class, Invoker.class, 
					new Convertlet<Invocable, Invoker>() {
				public Invoker convert(Invocable from) {
					return new ScriptInvoker(from);				
				}
			});
		}
	}
	
	private final Invocable invocable;
	
	public ScriptInvoker(Invocable invocable) {
		if (invocable == null) {
			throw new NullPointerException("No Invokable");
		}
		this.invocable = invocable;
	}
	
	@Override
	public Object invoke(String name, InvokerArguments arguments) 
	throws ScriptException, NoSuchMethodException {
										
		Object args[] = new Object[arguments.size()];
		
		for (int i = 0; i < args.length; ++i) {
			try {
				args[i] = arguments.getArgument(i, Object.class);
			} catch (ArooaConversionException e) {
				throw new RuntimeException("Failed converting arg " + i, e);
			}
		}
		
		return invocable.invokeFunction(name, args);
	}	
}
