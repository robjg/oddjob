package org.oddjob.script;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.framework.SerializableJob;
import org.oddjob.jmx.JMXServiceJob;

/**
 * 
 * @oddjob.description Invoke a java method or script snippet.
 * <p>
 * This is a wrapper for {@link InvokeType}. The result of the
 * invocation is placed in the <code>result</code> property.
 * 
 * @oddjob.example
 * 
 * Invoking a method on a bean.
 * 
 * {@oddjob.xml.resource org/oddjob/script/InvokeJobMethod.xml}
 *
 * Where <code>EchoService</code> is:
 * 
 * {@oddjob.java.resource org/oddjob/script/EchoService.java}
 * 
 * @oddjob.example
 *
 * Examples elsewhere. The {@link JMXServiceJob} job has an example of 
 * invoking a JMX operation.
 * 
 * @author rob
 *
 */
public class InvokeJob extends SerializableJob {
	private static final long serialVersionUID = 2012080600L;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The java object or script Invocable on
	 * which to invoke the method/function.
	 * @oddjob.required Yes.
	 */
	private transient Invoker source;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The function/method to call. 
	 * @oddjob.required Yes.
	 */
	private String function;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The values to use as arguments. 
	 * @oddjob.required Must match the expected arguments.
	 */
	private transient List<ArooaValue> parameters;	
	
	/** The result of the invocation. */
	private transient Object result;
	
	/**
	 * Constructor.
	 */
	public InvokeJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		parameters = new ArrayList<ArooaValue>();
	}
	
	
	@Override
	protected int execute() throws Throwable {
		
		InvokeType delegate = new InvokeType();
		delegate.setArooaSession(getArooaSession());
		delegate.setFunction(function);
		delegate.setSource(source);
		
		for (int i = 0; i < parameters.size(); ++i) {
			delegate.setParameters(i, parameters.get(i));
		}
		
		result = delegate.toValue();
		
		return 0;
	}

	@Override
	protected void onReset() {
		result = null;
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
	
	public Object getResult() {
		return result;
	}
	
	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
		if (result instanceof Serializable) {
			s.writeObject(result);
		}
		else {
			s.writeObject(null);
		}
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		result = s.readObject();
		completeConstruction();
	}
}
