package org.oddjob.jmx.server;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.MockClassResolver;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.Utils;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.handlers.RunnableHandlerFactory;
import org.oddjob.util.MockThreadManager;

public class ObjectMBeanServerInfoTest extends OjTestCase {

	private class OurClassResolver extends MockClassResolver {
		
		@Override
		public Class<?> findClass(String className) {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private class OurServerSession extends MockServerSession {
		
		ArooaSession session = new StandardArooaSession();
		
		@Override
		public ArooaSession getArooaSession() {
			return session;
		}
	}
	
	private class OurDirectory extends MockBeanRegistry {
	
		@Override
		public String getIdFor(Object component) {
			return "x";
		}
	}
	
	/**
	 * Test creating and registering an OddjobMBean.
	 */
   @Test
	public void testServerInfo() 
	throws Exception {
		Runnable myJob = new Runnable() {
			public void run() {
				
			}
		};

		ServerInterfaceManagerFactoryImpl imf = 
			new ServerInterfaceManagerFactoryImpl();
		
		imf.addServerHandlerFactories(
				new ServerInterfaceHandlerFactory[] { 
						new RunnableHandlerFactory()});
		
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				imf);

		ServerContext serverContext = new ServerContextImpl(
				myJob, sm, new OurDirectory());
		
		final OddjobMBean ojmb = new OddjobMBean(
				myJob, OddjobMBeanFactory.objectName(0),
				new OurServerSession(), 
				serverContext);
		
		RemoteOddjobBean  rob = (RemoteOddjobBean) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] { RemoteOddjobBean.class }, 
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return ojmb.invoke(method.getName(), args, 
								Utils.classArray2StringArray(method.getParameterTypes()));
					}			
		});
		
		ServerInfo info = rob.serverInfo();
		
		assertEquals("url", "//test:x", info.getAddress().toString());		

		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		for (int i = 0; i < info.getClientResolvers().length; ++i) {
			ClientHandlerResolver<?> resolver = info.getClientResolvers()[i];
			interfaces.add(resolver.resolve(new OurClassResolver()).interfaceClass());
		}
		assertTrue("runnable", interfaces.contains(Runnable.class));
	}
}
