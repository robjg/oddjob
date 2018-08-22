package org.oddjob.monitor.model;

import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.KeyStroke;

import org.oddjob.monitor.context.ExplorerContext;

import org.oddjob.OjTestCase;

public class JobActionTest extends OjTestCase {

   @Test
	public void testEnabledPropertyNotification() {
		
		class MyAction extends JobAction {

			@Override
			protected void doPrepare(ExplorerContext explorerContext) {
			}
			
			@Override
			protected void doFree(ExplorerContext explorerContext) {
			}
			
			@Override
			protected void doAction() throws Exception {
				// TODO Auto-generated method stub
				
			}

			public String getName() {
				return null;
			}

			public String getGroup() {
				return null;
			}
			
			public Integer getMnemonicKey() {
				throw new RuntimeException("Unexpected.");
			}
			
			public KeyStroke getAcceleratorKey() {
				throw new RuntimeException("Unexpected.");
			}
		}
		
		class MyPropertyListner implements PropertyChangeListener {
			boolean enabled;
			public void propertyChange(PropertyChangeEvent evt) {
				String propertyName = evt.getPropertyName();
				if (JobAction.ENABLED_PROPERTY.equals(propertyName)) {
					enabled = (Boolean) evt.getNewValue();
				}
			}
		}
		
		MyPropertyListner listener = new MyPropertyListner();
		
		MyAction test = new MyAction();
		test.setEnabled(false);
		
		test.addPropertyChangeListener(listener);
		
		assertFalse(listener.enabled);
		
		test.setEnabled(true);
		
		assertTrue(listener.enabled);
	}
}
