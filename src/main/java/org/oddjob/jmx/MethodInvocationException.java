package org.oddjob.jmx;

import java.lang.reflect.Method;

public class MethodInvocationException extends Exception {
	private static final long serialVersionUID = 20070312;
	
	public MethodInvocationException(String componentName, Method method, Object[] args, Throwable cause) {
		super("Failed invoking method [" + method.toString() + "] of component [" +
				componentName + "]\nArgs: " + args(args), cause);
	}
	
	static String args(Object[] args) {
		StringBuilder buf = new StringBuilder();
		
		buf.append('(');
		for (int i = 0; i < args.length; ++i) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append('[');
			buf.append(args[i] == null ? "null" : args[i]);
			buf.append(']');
		}
		buf.append(')');
		return buf.toString();
	}
}
