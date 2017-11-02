/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.action;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.MockConfigurationOwner;
import org.oddjob.arooa.parsing.MockConfigurationSession;
import org.oddjob.arooa.parsing.QTag;
import org.oddjob.arooa.standard.StandardArooaDescriptor;
import org.oddjob.designer.view.DummyDialogue;
import org.oddjob.designer.view.DummyFormViewFactory;
import org.oddjob.designer.view.SelectionWidget;
import org.oddjob.designer.view.TextWidget;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextInialiser;
import org.oddjob.monitor.model.MockExplorerContext;

public class SetPropertyActionTest extends OjTestCase {

	class RootContext extends MockExplorerContext {
		
		@Override
		public ExplorerContext getParent() {
			return null;
		}
	}
	
   @Test
	public void testRootContext() {
		
		SetPropertyAction test = new SetPropertyAction();
		
		test.setSelectedContext(new RootContext());
		test.prepare();
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());
		
		test.setSelectedContext(null);
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());
	}
	
	
	private class ParentContext extends MockExplorerContext {
		
		StandardArooaDescriptor descriptor = 
			new StandardArooaDescriptor();
		
		@Override
		public Object getValue(String key) {
			assertEquals(ConfigContextInialiser.CONFIG_OWNER, key);
			return new MockConfigurationOwner() {
				public ConfigurationSession provideConfigurationSession() {
					return new MockConfigurationSession() {
						@Override
						public ArooaDescriptor getArooaDescriptor() {
							return descriptor;
						}
					};
				}
			};
		}
	}
	
	class OurExplorerContext extends MockExplorerContext {

		Object component;
		
		@Override
		public Object getThisComponent() {
			return component;
		}
		
		@Override
		public ExplorerContext getParent() {
			return new ParentContext();
		}
		
	}
	
	public static class Component { 
		String fruit;
		public void setFruit(String fruit) {
			this.fruit = fruit;
		}
	}
	
   @Test
	public void testSetProperty() throws Exception {
		Component component = new Component();
				
		SetPropertyAction test = new SetPropertyAction();
		assertFalse(test.isEnabled());
		assertFalse(test.isVisible());
		
		OurExplorerContext ec = new OurExplorerContext();
		ec.component = component;
		
		test.setSelectedContext(ec);
		test.prepare();
		
		assertTrue(test.isEnabled());

		Form form = test.form();
		
		DummyDialogue dv = DummyFormViewFactory.create(form).dialogue();
		
		((TextWidget) dv.get("Name")).setText("fruit");
		
		SelectionWidget selection = (SelectionWidget) dv.get("Value");

		DesignInstance value = selection.setSelected(new QTag("value"));

		DummyDialogue valueDialog = DummyFormViewFactory.create(value.detail()).dialogue();
		
		((TextWidget) valueDialog.get(null)).setText("apples");
		
		test.action();
		
		assertEquals("apples", component.fruit);
	}

}
