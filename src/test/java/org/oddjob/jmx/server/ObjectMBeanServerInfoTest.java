package org.oddjob.jmx.server;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.MockClassResolver;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.Utils;
import org.oddjob.jmx.handlers.RunnableHandlerFactory;
import org.oddjob.util.MockThreadManager;

import java.lang.reflect.Proxy;
import java.util.Arrays;

public class ObjectMBeanServerInfoTest extends OjTestCase {

	private static class OurClassResolver extends MockClassResolver {
		
		@Override
		public Class<?> findClass(String className) {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class OurServerSession extends MockServerSession {
		
		ArooaSession session = new StandardArooaSession();
		
		@Override
		public ArooaSession getArooaSession() {
			return session;
		}
	}
	
	private static class OurDirectory extends MockBeanRegistry {
	
		@Override
		public String getIdFor(Object component) {
			return "x";
		}
	}
	
	/**
	 * Test creating and registering an OddjobMBean.
	 */
   @Test
	public void testServerInfo() {
		Runnable myJob = () -> {

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
				myJob, 0L,
				new OurServerSession(),
				serverContext);
		
		RemoteOddjobBean  rob = (RemoteOddjobBean) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] { RemoteOddjobBean.class },
				(proxy, method, args) -> ojmb.invoke(method.getName(), args,
						Utils.classArray2StringArray(method.getParameterTypes())));
		
		ServerInfo info = rob.serverInfo();
		
		assertEquals("url", "//test:x", info.getAddress().toString());		

		assertThat("runnable", Arrays.asList(info.getInterfaces()),
				Matchers.hasItem(Runnable.class.getName()));
	}
}
