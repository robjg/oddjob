package org.oddjob.jmx.handlers;

/**
 * An exception that can be sent across the wire without the class that
 * caused the exception being available at the client. This exception
 * still carries the message and all the stack trace information from the 
 * original exception so a problem can be diagnosed.
 * 
 * @author rob
 *
 */
public class OddjobTransportableException extends Exception {
	private static final long serialVersionUID = 2012032200L; 

	private final String originalExceptionClassName;
	
	public OddjobTransportableException(Throwable t) {
		super(t.getMessage());
		this.originalExceptionClassName = t.getClass().getName();
		setStackTrace(t.getStackTrace());
		if (t.getCause() != null) {
			initCause(new OddjobTransportableException(t.getCause()));
		}
	}

	private OddjobTransportableException(String originalExceptionClassName,
										 String message,
										 StackTraceElement[] stackTraceElements,
										 OddjobTransportableException cause) {
		super(message);
		this.originalExceptionClassName = originalExceptionClassName;
		setStackTrace(stackTraceElements);
		if (cause != null) {
			initCause(cause);
		}
	}

	public static OddjobTransportableException from(Throwable throwable) {

		if (throwable == null) {
			return null;
		}
		else if (throwable instanceof OddjobTransportableException) {
			return (OddjobTransportableException) throwable;
		}
		else {
			return new OddjobTransportableException(throwable);
		}
	}


	public static OddjobTransportableException fromBean(AsBean bean) {

		if (bean == null) {
			return null;
		}

		StackElement[] stackElements = bean.getStackElements();
		StackTraceElement[] stackTraceElements = new StackTraceElement[stackElements.length];
		for (int i = 0; i < stackTraceElements.length; ++i) {
			StackElement stackElement = stackElements[i];
			stackTraceElements[i] = new StackTraceElement(
					stackElement.getDeclaringClass(),
					stackElement.getMethodName(),
					stackElement.getFileName(),
					stackElement.getLineNumber());
		}

		OddjobTransportableException cause = fromBean(bean.getCause());

		return new OddjobTransportableException(bean.getOriginalExceptionClassName(),
				bean.getMessage(), stackTraceElements, cause);

	}

	public static AsBean toBean(OddjobTransportableException oddjobTransportableException) {

		if (oddjobTransportableException == null) {
			return null;
		}
		else {
			return oddjobTransportableException.toBean();
		}
	}

	
	public String getOriginalExceptionClassName() {
		return originalExceptionClassName;
	}

	public AsBean toBean() {

		StackTraceElement[] stackTraceElements = getStackTrace();

		StackElement[] stackElements = new StackElement[stackTraceElements.length];
		for (int i = 0; i < stackTraceElements.length; ++i) {
			StackTraceElement stackTraceElement = stackTraceElements[i];
			stackElements[i] = new StackElement(
					stackTraceElement.getClassName(),
					stackTraceElement.getMethodName(),
					stackTraceElement.getFileName(),
					stackTraceElement.getLineNumber());
		}

		return new AsBean(originalExceptionClassName,
				getMessage(),
				stackElements,
				toBean(getCause()));
	}

	@Override
	public OddjobTransportableException getCause() {
		return (OddjobTransportableException) super.getCause();
	}

	@Override
	public String toString() {
        String message = getMessage();
        return (message != null) ? 
        		(originalExceptionClassName + ": " + message) :
				originalExceptionClassName;
	}

	public static class AsBean {

		private final String originalExceptionClassName;

		private final String message;

		private final StackElement[] stackElements;

		private final AsBean cause;

		public AsBean(String originalExceptionClassName, String message, StackElement[] stackElements, AsBean cause) {
			this.originalExceptionClassName = originalExceptionClassName;
			this.message = message;
			this.stackElements = stackElements;
			this.cause = cause;
		}

		public String getOriginalExceptionClassName() {
			return originalExceptionClassName;
		}

		public String getMessage() {
			return message;
		}

		public StackElement[] getStackElements() {
			return stackElements;
		}

		public AsBean getCause() {
			return cause;
		}
	}

	public static class StackElement {

		private final String declaringClass;

		private final String methodName;

		private final String fileName;

		private final int lineNumber;

        public StackElement(String declaringClass, String methodName, String fileName, int lineNumber) {
            this.declaringClass = declaringClass;
            this.methodName = methodName;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

		public String getDeclaringClass() {
			return declaringClass;
		}

		public String getMethodName() {
			return methodName;
		}

		public String getFileName() {
			return fileName;
		}

		public int getLineNumber() {
			return lineNumber;
		}
	}

}
