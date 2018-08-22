package org.oddjob.jmx;

/**
 * A Base class that encapsulates the idea of an Operation that
 * can be invoked locally or remotely.
 * <p>
 * This is not an interface to allow a common idea of equality
 * based on method name and signature to be defined.
 *  
 * @author rob
 *
 * @param <T> The return type.
 */
public abstract class RemoteOperation<T> {

	abstract public String getActionName();
	
	abstract public String[] getSignature();
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (! (obj instanceof RemoteOperation<?>)) {
			return false;
		}
		
		RemoteOperation<?> other = (RemoteOperation<?>) obj;
		
		if (!getActionName().equals(other.getActionName())) {
			return false;
		}
		
		String[] signature = getSignature();
		String[] otherSig = other.getSignature();
		
		if (signature.length != otherSig.length) {
			return false;
		}
		
		for (int i = 0; i < signature.length; ++i) {
			if (!signature[i].equals(otherSig[i])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return getActionName().hashCode() + getSignature().length;
	}
	
	public String toString() {
		
		StringBuffer buf = new StringBuffer();
		String[] sig = getSignature();
		buf.append('(');
		for (int i = 0; i < sig.length; ++i) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(sig[i]);
		}
		buf.append(')');
		
		return "RemoteOperation " 
			+ getActionName() 
			+ buf.toString();
	}
}
