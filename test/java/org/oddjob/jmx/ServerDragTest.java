package org.oddjob.jmx;

import java.util.concurrent.atomic.AtomicReference;

import org.custommonkey.xmlunit.XMLTestCase;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.CutAndPasteSupport;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;

public class ServerDragTest extends XMLTestCase {

	XMLConfiguration configuration = new XMLConfiguration(
			"TEST", "<oddjob id='apples'>" +
					"<job>" +
					" <echo id='colour'>red</echo>" +
					"</job>" +
					"</oddjob>");
		
	final AtomicReference<String > savedXML = new AtomicReference<String>();
	
	{ 
		configuration.setSaveHandler(new XMLConfiguration.SaveHandler() {
			@Override
			public void acceptXML(String xml) {
				savedXML.set(xml);
			}
		});
	}
	
	JMXServerJob server;
	JMXClientJob client;
	
	ConfigurationOwner remoteOddjob;
	
	class OurContext extends MockArooaContext {
		
		ArooaSession session;
		
		public OurContext(ArooaSession  session) {
			this.session = session; 
		}
		
		
		@Override
		public RuntimeConfiguration getRuntime() {
			return new MockRuntimeConfiguration() {
				@Override
				public void configure() {
				}
			};
		}
		
		@Override
		public ArooaSession getSession() {
			return session;
		}
	}
	
	@Override
	protected void setUp() throws Exception {

		ArooaSession serverSession = new OddjobSessionFactory().createSession();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(configuration);
		
		ComponentPool serverPool = serverSession.getComponentPool(); 
		
		serverPool.registerComponent(
				new ComponentTrinity(
						oddjob, 
						oddjob, 
						new OurContext(serverSession)), 
				"main");
		
		oddjob.setArooaSession(serverSession);
		
		oddjob.run();
		
		
		server = new JMXServerJob();
		server.setArooaSession(serverSession);
		server.setUrl("service:jmx:rmi://");
		server.setRoot(oddjob);
		
		server.start();
		
		
		ArooaSession clientSession = new StandardArooaSession();
				
		client = new JMXClientJob();
		client.setConnection(server.getAddress());
		client.setArooaSession(clientSession);
		
		ComponentPool clientPool = clientSession.getComponentPool();
		
		clientPool.registerComponent(
				new ComponentTrinity(
						client, 
						client, 
						new OurContext(clientSession)), 
				null);
		
		client.run();
		
		remoteOddjob = (ConfigurationOwner) new OddjobLookup(client).lookup("main");
	}
	
	@Override
	protected void tearDown() throws Exception {
		client.stop();
		server.stop();
	}
	
	String EOL = System.getProperty("line.separator");
	
	
	public void testCutLeaf() throws Exception {
		
		assertNotNull(remoteOddjob);
		
		Object toCut = new OddjobLookup(client).lookup("main/colour");
		assertNotNull(toCut);
		
		DragPoint dragPoint = remoteOddjob.provideConfigurationSession().dragPointFor(toCut);
		
		DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
		dragPoint.cut();
		trn.commit();
		
		remoteOddjob.provideConfigurationSession().save();
		
		String expected = "<oddjob id=\"apples\"/>" + EOL;
		
		assertXMLEqual(expected, savedXML.get());
	}
	
	public void testEditRoot() throws Exception {
		
		assertNotNull(remoteOddjob);
		
		Object toEdit = new OddjobLookup(client).lookup("main");
		assertNotNull(toEdit);
		
		DragPoint dragPoint = remoteOddjob.provideConfigurationSession().dragPointFor(toEdit);
		
		XMLArooaParser parser = new XMLArooaParser();
		
		ConfigurationHandle handle = parser.parse(dragPoint);
		
		String replacement =  
			"<oddjob id=\"oranges\">" + EOL +
			"    <job>" + EOL +
			"        <echo id=\"colour\"><![CDATA[orange]]></echo>" + EOL +
			"    </job>" + EOL +
			"</oddjob>" + EOL;

		CutAndPasteSupport.replace(
				handle.getDocumentContext().getParent(), 
				handle.getDocumentContext(), 
				new XMLConfiguration(
					"REPLACEMENT", replacement));
		
		handle.save();

		assertNull(savedXML.get());
		
		remoteOddjob.provideConfigurationSession().save();
		
		assertXMLEqual(replacement, savedXML.get());
	}
	
	public void testPaste() throws Exception {
		
		testCutLeaf();
		
		Object pastePoint = new OddjobLookup(client).lookup("main");
		assertNotNull(pastePoint);
		
		DragPoint dragPoint = remoteOddjob.provideConfigurationSession().dragPointFor(pastePoint);
		
		String paste = 
			"<echo id=\"colour\"><![CDATA[orange]]></echo>";

		DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
		dragPoint.paste(0, paste);
		trn.commit();
		
		remoteOddjob.provideConfigurationSession().save();
		
		String expected =  
			"<oddjob id=\"apples\">" + EOL +
			"    <job>" + EOL +
			"        <echo id=\"colour\"><![CDATA[orange]]></echo>" + EOL +
			"    </job>" + EOL +
			"</oddjob>" + EOL;
		
		assertXMLEqual(expected, savedXML.get());
	}
	
	public void testFailedPaste() throws ArooaParseException {

		// No Cut!
		
		Object pastePoint = new OddjobLookup(client).lookup("main");
		assertNotNull(pastePoint);
		
		DragPoint dragPoint = remoteOddjob.provideConfigurationSession().dragPointFor(pastePoint);
		
		String paste = 
			"<echo id=\"colour\"" +
			"      text=\"orange\"/>";

		try {
			dragPoint.paste(0, paste);
			fail("Should fail because of two nodes.");
		} catch (Exception e) {
			// expected
		}
	}
}
