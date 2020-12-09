package org.oddjob.monitor;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.design.designer.ArooaTree;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.view.ExplorerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public class OddjobExplorerTest extends OjTestCase {

	private Logger logger = LoggerFactory.getLogger(OddjobExplorerTest.class);
	
    @Before
    public void setUp() throws Exception {
		
		logger.info("----------------  " + getName() + "  --------------");
		
		if (Thread.currentThread().getContextClassLoader() == null) {
			
			// https://github.com/VRL-Studio/VRL-Studio/issues/11
			// When running from ant in single JVM mode.
			// - although this has just happened in Fork Mode too!!!
			logger.warn("What is setting Context Class Loader to Null?");			
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); 
		}

	}
	
   @Test
	public void testSave() throws SAXException, IOException, PropertyVetoException {
		
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <echo><![CDATA[Hello]]></echo>" +
			" </job>" +
			"</oddjob>";
		
		XMLConfiguration config = new XMLConfiguration(
				"TEST", xml);
		
		final AtomicReference<String > savedXML = new AtomicReference<String>();
		config.setSaveHandler(new XMLConfiguration.SaveHandler() {
			@Override
			public void acceptXML(String xml) {
				savedXML.set(xml);
			}
		});
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		oddjob.load();
		
		OddjobExplorer test = new OddjobExplorer();
		test.setArooaSession(new StandardArooaSession());
		test.createView();
		
		test.addPropertyChangeListener(test.new ChangeFocus());
		test.addPropertyChangeListener(test.new ChangeView());
		
		test.setOddjob(oddjob);
		
		ExplorerComponent component = test.getExplorerComponent();
		
		component.getTree().setSelectionRow(1);
		
		assertNull(savedXML.get());
		
		Action action = test.new SaveAction();
		action.actionPerformed(null);

		
		Diff diff = DiffBuilder.compare(savedXML.get())
				.withTest(xml).ignoreWhitespace()
				.build();

		assertFalse(diff.toString(), diff.hasDifferences());
	}
	
	public static class OurConfigOwner extends MockConfigurationOwner {
		
		SessionStateListener listener;
		
		public ConfigurationSession provideConfigurationSession() {
			return new MockConfigurationSession() {
				
				@Override
				public boolean isModified() {
					return false;
				}
				
				@Override
				public void addSessionStateListener(
						SessionStateListener listener) {
					assertNull(OurConfigOwner.this.listener);
					OurConfigOwner.this.listener = listener;
				}
				
				@Override
				public void removeSessionStateListener(
						SessionStateListener listener) {
					assertEquals(OurConfigOwner.this.listener, listener);
					OurConfigOwner.this.listener = null;
				}
			};
		}
		
		@Override
		public void addOwnerStateListener(OwnerStateListener listener) {
		}
		
		@Override
		public void removeOwnerStateListener(OwnerStateListener listener) {
		}
		
		@Override
		public String toString() {
			return "oranges";
		}
	}
	
   @Test
	public void testTitle() throws Exception {
		
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}

		String xml = 
				"<oddjob>" +
						" <job>" +
						"  <bean id='x' class='" + OurConfigOwner.class.getName() + "'/>" +
						" </job>" +
						"</oddjob>";

		XMLConfiguration config = new XMLConfiguration(
				"TEST", xml);

		Oddjob oddjob = new Oddjob();
		oddjob.setName("apples");
		oddjob.setConfiguration(config);
		oddjob.load();

		OddjobExplorer test = new OddjobExplorer();
		test.setArooaSession(new StandardArooaSession());
		test.createView();

		test.addPropertyChangeListener(test.new ChangeFocus());
		test.addPropertyChangeListener(test.new ChangeView());

		assertEquals("Oddjob Explorer", test.getTitle());

		test.setOddjob(oddjob);

		ExplorerComponent component = test.getExplorerComponent();

		assertEquals("Oddjob Explorer - apples", test.getTitle());

		final JTree tree = component.getTree();

		assertEquals(false, tree.isExpanded(0));

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				tree.expandRow(0);
				tree.setSelectionRow(1);		
			}
		});
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				// Wait for event queue to drain.
			}
		});

		assertEquals(false, tree.isSelectionEmpty());

		assertEquals("Oddjob Explorer - oranges", test.getTitle());

		OurConfigOwner owner = (OurConfigOwner) new OddjobLookup(
				oddjob).lookup("x");

		owner.listener.sessionModified(
				new ConfigSessionEvent(
						new MockConfigurationSession()));

		assertEquals("Oddjob Explorer - oranges *", test.getTitle());

		final DragPoint dragPoint = oddjob.provideConfigurationSession().dragPointFor(
				owner);

		final AtomicReference<Exception> er = new AtomicReference<Exception>();

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
				dragPoint.delete();
				try {
					trn.commit();
				} catch (ArooaParseException e) {
					trn.rollback();
					er.set(e);
				}
			}
		});

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				// Wait for event queue to drain.
			}
		});

		if (er.get() != null) {
			throw er.get();
		}

		assertEquals("Oddjob Explorer - apples *", test.getTitle());
	}
	
   @Test
	public void testNewOddjob() throws SAXException, IOException, PropertyVetoException, ArooaParseException, InterruptedException, InvocationTargetException {
		
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		
		OddjobExplorer test = new OddjobExplorer();
		test.setArooaSession(new OddjobSessionFactory().createSession());
		test.createView();
		
		test.addPropertyChangeListener(test.new ChangeFocus());
		test.addPropertyChangeListener(test.new ChangeView());
		
		assertEquals("Oddjob Explorer", test.getTitle());
		
		test.new NewAction().actionPerformed(null);
		
		assertEquals("Oddjob Explorer - Oddjob", test.getTitle());
		
		final ArooaTree tree = (ArooaTree) test.getExplorerComponent().getTree();
			
		tree.setSelectionRow(0);

//		final JobTreeNode node = (JobTreeNode) tree.getSelectionPath().getLastPathComponent();		
		
		SwingUtilities.invokeAndWait(new Runnable() {
			
			public void run() {

				DragPoint dragPoint = tree.getCurrentDragPoint();
				
				try {
					DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
					dragPoint.paste(0, "<bean/>");
					trn.commit();
				} catch (ArooaParseException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		assertEquals("Oddjob Explorer - Oddjob *", test.getTitle());
	}
	
   @Test
	public void testResetOddjob() throws PropertyVetoException, ArooaParseException {
		
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		
		if (Thread.currentThread().getContextClassLoader() == null) {
			// What does this? HSQL? 
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		}
		
		String xml = 
			"<oddjob/>";
		
		XMLConfiguration config = new XMLConfiguration(
				"TEST", xml);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setName("apples");
		oddjob.setConfiguration(config);
		oddjob.load();
		
		OddjobExplorer test = new OddjobExplorer();
		test.setArooaSession(new StandardArooaSession());
		test.createView();
		
		test.addPropertyChangeListener(test.new ChangeFocus());
		test.addPropertyChangeListener(test.new ChangeView());
		
		test.setOddjob(oddjob);
		
		assertEquals("Oddjob Explorer - apples", test.getTitle());
		
		DragPoint dragPoint = oddjob.provideConfigurationSession().dragPointFor(
				oddjob);
		
		// Paste Inside.
		DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
		dragPoint.paste(0, "<bean/>");
		trn.commit();
		
		assertEquals("Oddjob Explorer - apples *", test.getTitle());
		
		
		oddjob.hardReset();
		
		assertEquals("Oddjob Explorer - apples", test.getTitle());
		
		oddjob.destroy();
	}
	
   @Test
	public void testCheckModifications() throws PropertyVetoException, ArooaParseException, InterruptedException, InvocationTargetException {
		
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		
		final OddjobExplorer test = new OddjobExplorer();
		test.setArooaSession(new StandardArooaSession());
		test.createView();
		
		final boolean[] checked = new boolean[1];
		
		test.addPropertyChangeListener(test.new ChangeView());
		test.vetoableChangeSupport.addVetoableChangeListener(
				test.new CheckConfigurationsSaved() {
					@Override
					boolean canClose(Collection<ConfigurationOwner> modified) {
						assertEquals(1, modified.size());
						checked[0] = true;
						return true;
					}
				});
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {

				test.new NewAction().actionPerformed(null);

				Oddjob oddjob = test.getOddjob();
				
				DragPoint dragPoint = oddjob.provideConfigurationSession().dragPointFor(
						oddjob);
				
				// Paste Inside.
				try {				
					DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
					dragPoint.paste(0, "<bean/>");
					trn.commit();
				} catch (ArooaParseException e) {
					throw new RuntimeException(e);
				}
			}
		});

		test.setOddjob(null);
		
		assertTrue(checked[0]);
	}
	
   @Test
	public void testModifiedOnCloseAction() throws SAXException, IOException, PropertyVetoException, ArooaParseException, InterruptedException, InvocationTargetException {
				
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean/>" +
			" </job>" +
			"</oddjob>";
		
		XMLConfiguration config = new XMLConfiguration(
				"TEST", xml);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		oddjob.load();
		
		OddjobExplorer test = new OddjobExplorer();
		test.setArooaSession(new OddjobSessionFactory().createSession());
		test.createView();
		
		test.addPropertyChangeListener(test.new ChangeFocus());
		test.addPropertyChangeListener(test.new ChangeView());
		
		assertEquals("Oddjob Explorer", test.getTitle());
		
		test.setOddjob(oddjob);
		
		assertEquals("Oddjob Explorer - Oddjob", test.getTitle());
		
		test.new CloseAction().actionPerformed(null);
		
		assertEquals("Oddjob Explorer", test.getTitle());
	}

}
