package org.oddjob.monitor.action;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ExplorerContextImpl;
import org.oddjob.monitor.model.ExplorerModelImpl;
import org.oddjob.util.SimpleThreadManager;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExecuteActionTest2 {

	class MyClassLoader extends ClassLoader {
		
	}
	
	public static class ClassLoaderCapture implements Runnable {
		
		ClassLoader context;
	
		ClassLoader result;
		
		public void run() {
			context = Thread.currentThread().getContextClassLoader();
		}
		
		@Inject
		public void setResult(ClassLoader result) {
			this.result = result;
		}
		
		public ClassLoader getResult() {
			return result;
		}
		
		public ClassLoader getContext() {
			return context;
		}

	}
	
	@Test
	public void testClassLoader() throws Exception {

		ClassLoader startLoader = Thread.currentThread().getContextClassLoader();
		
 		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='capture'" +
			"         class='" + ClassLoaderCapture.class.getName() + "'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		MyClassLoader loader = new MyClassLoader();
		
		ExecuteAction test = new ExecuteAction();

		ExplorerModelImpl eModel = new ExplorerModelImpl(
				new StandardArooaSession());
		eModel.setThreadManager(new SimpleThreadManager());
		eModel.setOddjob(oddjob);
		
		ExplorerContextImpl rootContext = new ExplorerContextImpl(eModel);
		
		oddjob.setClassLoader(loader);
		
		oddjob.run();
		
		assertTrue(startLoader == Thread.currentThread().getContextClassLoader());

		Object capture = 
			new OddjobLookup(oddjob).lookup("capture");
		((Resettable) capture).hardReset();
		
		ExplorerContext ourContext = rootContext.addChild(capture);
		
		test.setSelectedContext(ourContext);
		
		test.action();
		
		ClassLoader result = (ClassLoader) PropertyUtils.getProperty(
				capture, "result");
		
		assertEquals(loader, result);
		
		ClassLoader context = (ClassLoader) PropertyUtils.getProperty(
				capture, "context");
		
		assertEquals(startLoader, context);

	}
}
