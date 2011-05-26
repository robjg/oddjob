package org.oddjob.persist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

public class OddjobObjectInputStream extends ObjectInputStream{

	private final ClassLoader classLoader;
	
	public OddjobObjectInputStream(InputStream inputStream,
			ClassLoader classLoader) throws IOException {
		super(inputStream);
		this.classLoader = classLoader;
	}
	
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		String className = desc.getName();
		return Class.forName(className, true, classLoader);
	}
	
	@Override
	protected Class<?> resolveProxyClass(String[] interfaces)
			throws IOException, ClassNotFoundException {

		Class<?>[] classObjs = new Class[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
		    Class<?> cl = Class.forName(interfaces[i], false, classLoader);
		    classObjs[i] = cl;
		}
		
		return Proxy.getProxyClass(
			classLoader,
			classObjs);
	}
}
