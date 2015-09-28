package org.oddjob.persist;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.oddjob.io.BufferType;
import org.oddjob.oddballs.BuildOddballs;
import org.oddjob.tools.OurDirs;

public class OddjobObjectInputStreamTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		
		new BuildOddballs().run();
	}
	
	/**
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public void testNonSystemClassLoaderDeserialisation() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		OurDirs dirs = new OurDirs();
		
		File file = new File(dirs.base(), "test/oddballs/apple/classes/");
		
		URL[] urls = { file.toURI().toURL() };
		
		URLClassLoader test = new URLClassLoader(urls,
				getClass().getClassLoader());
						
		Class<?> appleClass = Class.forName("fruit.Apple", true, test);
		
		Object apple = appleClass.newInstance();
		
		BufferType buffer = new BufferType();
		buffer.configured();
		
		ObjectOutputStream oo = new ObjectOutputStream(buffer.toOutputStream());
		
		oo.writeObject(apple);
		
		OddjobObjectInputStream oi = new OddjobObjectInputStream(
				buffer.toInputStream(), test);
		
		Object copy = oi.readObject();
		
		oi.close();
		
		assertEquals(appleClass, copy.getClass());
	}

	private static class OurHandler implements InvocationHandler, Serializable {
		private static final long serialVersionUID = 2009092300L;
		
		String methodName;
		
		public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable {
			methodName = method.getName();
			return null;
		}

	}
	
	
	/**
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public void testNonSystemClassLoaderProxyDeserialisation() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

		OurDirs dirs = new OurDirs();
		
		File file = new File(dirs.base(), "test/oddballs/apple/classes/");
		
		URL[] urls = { file.toURI().toURL() };
		
		URLClassLoader test = new URLClassLoader(urls, 
				this.getClass().getClassLoader());
						
		Class<?> appleClass = Class.forName("fruit.Fruit", true, test);		
		
		Object proxy = Proxy.newProxyInstance(test, new Class<?>[] {
				appleClass },
				new OurHandler());
		
		BufferType buffer = new BufferType();
		buffer.configured();
		
		ObjectOutputStream oo = new ObjectOutputStream(buffer.toOutputStream());
		
		oo.writeObject(proxy);
		
		OddjobObjectInputStream oi = new OddjobObjectInputStream(
				buffer.toInputStream(), test);
		
		Object copy = oi.readObject();
		
		oi.close();
		
		assertTrue(appleClass.isInstance(copy));
		
		Method m = appleClass.getDeclaredMethod("getColour");
		
		m.invoke(copy);
		
		OurHandler handler = (OurHandler) Proxy.getInvocationHandler(copy);
		
		assertEquals("getColour", handler.methodName);
	}


}
