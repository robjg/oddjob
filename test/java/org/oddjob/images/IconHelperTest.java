package org.oddjob.images;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import junit.framework.TestCase;

import org.oddjob.Iconic;

public class IconHelperTest extends TestCase {

	class OurListener implements IconListener {
		
		List<IconEvent> events = new ArrayList<IconEvent>();
		@Override
		public void iconEvent(IconEvent e) {
			events.add(e);
		}
	}
	
	class OurIconic implements Iconic {
		@Override
		public void addIconListener(IconListener listener) {
		}
		@Override
		public void removeIconListener(IconListener listener) {
		}
		@Override
		public ImageIcon iconForId(String id) {
			throw new RuntimeException();
		}
	}
	
	public void testSameIdNotFired() {
		
		IconHelper test = new IconHelper(new OurIconic());

		OurListener listener = new OurListener();
		
		test.addIconListener(listener);
		
		assertEquals("ready", listener.events.get(0).getIconId());
		assertEquals(1, listener.events.size());
		
		test.changeIcon("executing");
		
		assertEquals("executing", listener.events.get(1).getIconId());
		assertEquals(2, listener.events.size());
		
		test.changeIcon("executing");
		
		assertEquals("executing", listener.events.get(1).getIconId());
		assertEquals(2, listener.events.size());
		
	}
	
}
