package org.oddjob.script;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.jmx.JMXServiceJob;

/**
 * 
 * @oddjob.description Invoke a java method or script snippet.
 * <p>
 * This is a wrapper for {@link InvokeType}. The result of the
 * invocation is placed in the <code>result</code> property.
 * <p>
 * Note that stopping this job will simply attempt to interrupt the
 * thread invoking the method. The outcome of this will obviously vary.
 * <p>
 * Oddjob will do it's best to convert arguments to the signature of
 * the method or operation. An exception will result if it can't achieve 
 * this.
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
 * Invoking a static method. Note that this uses args instead of parameters
 * for convenience.
 * 
 * {@oddjob.xml.resource org/oddjob/script/InvokeJobStatic.xml}
 * 
 * @oddjob.example
 *
 * Examples elsewhere. 
 * <p>
 * See {@link InvokeType} for several more examples. Property configuration
 * is the same for the type and the job.
 * <p>
 * The {@link JMXServiceJob} job has an example of 
 * invoking a JMX operation.
 * 
 * @author rob
 *
 */
public class InvokeJob extends SerializableJob 
implements Stoppable {
	
	private static final long serialVersionUID = 2012080600L;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The java object or script Invocable on
	 * which to invoke the method/function. If the method is a Java static 
	 * method then this is the class on which to invoke the method.
	 * @oddjob.required Yes.
	 */
	private transient Invoker source;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The function/method/operation name to call. Note
	 * that for a Java static method the method name must be prefixed with
	 * the word static (see InvokeType examples).
	 * @oddjob.required Yes.
	 */
	private String function;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The values to use as arguments. Note that the
	 * <code>args</code> property may be more convenient for simple arguments.
	 * @oddjob.required Must match the expected arguments.
	 */
	private transient List<ArooaValue> parameters;	
	
	/**
	 * @oddjob.property
	 * @oddjob.description An alternative configuration for the values to use 
	 * as arguments. This was added for convenience as setting up a lot
	 * of simple arguments can be tedious. If this property is provided then
	 * parameters is ignored.
	 * @oddjob.required Must match the expected arguments.
	 */
	private transient Object[] args;
	
	/** The result of the invocation. */
	private transient Object result;
	
	private transient volatile Thread executingThread;
	
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
		delegate.setArgs(args);
		
		for (int i = 0; i < parameters.size(); ++i) {
			delegate.setParameters(i, parameters.get(i));
		}
		
		executingThread = Thread.currentThread();
		
		try {
			result = delegate.toValue();
		}
		finally {
			executingThread = null;
		}
		
		return 0;
	}

	@Override
	protected void onReset() {
		result = null;
	}
	
	@Override
	protected void onStop() throws FailedToStopException {
		Thread thread = executingThread;
		if (thread != null) {
			logger().info("Interrupting Invoke operation.");
			thread.interrupt();
		}
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
	
	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
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
