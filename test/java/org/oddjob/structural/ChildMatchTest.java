package org.oddjob.structural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.MockStructural;

public class ChildMatchTest extends TestCase {

	private static final Logger logger = Logger.getLogger(
			ChildMatchTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.debug("-------------------  " + getName() + "  -----------------");
	}
	
	private class OurListener implements StructuralListener {
		
		List<String> events = new ArrayList<String>();
	
		private boolean started;
		
		@Override
		public void childAdded(StructuralEvent event) {
			if (!started) {
				return;
			}
			String text = "Added " + event.getChild() + " at " + event.getIndex();
			events.add(text);
			logger.debug(text);
		}
		
		@Override
		public void childRemoved(StructuralEvent event) {
			if (!started) {
				return;
			}
			String text = "Removed " + event.getChild() + " from " + event.getIndex();
			events.add(text);
			logger.debug(text);
		}
		
		public void start() {
			started = true;
		}
	}
	
	private class OurChildMatch extends ChildMatch<String>{
		
		private final ChildHelper<String> childHelper;
		
		public OurChildMatch(ChildHelper<String> childHelper) {
			super(new ArrayList<String>(Arrays.asList(childHelper.getChildren(new String[0]))));
			this.childHelper = childHelper;
		}
		
		@Override
		protected void insertChild(int index, String child) {
			childHelper.insertChild(index, child);
		}
		
		@Override
		protected void removeChildAt(int index) {
			childHelper.removeChildAt(index);
		}
	}
	
	
	public void testExactMatch() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
				
		ChildMatch<String> test = new OurChildMatch(childHelper);

		String[] desired = { "red", "green", "blue" };
		test.match(desired);
		
		assertEquals(0, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testReversed() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);
		
		String[] desired = { "blue", "green", "red" };
		test.match(desired);
		
		assertEquals("Removed red from 0", listener.events.get(0));
		assertEquals("Removed green from 0", listener.events.get(1));
		assertEquals("Added green at 1", listener.events.get(2));
		assertEquals("Added red at 2", listener.events.get(3));
		
		assertEquals(4, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testInsertedAtBeginning() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);
		
		String[] desired = { "pink", "red", "green", "blue" };
		test.match(desired);
		
		assertEquals("Added pink at 0", listener.events.get(0));
		
		assertEquals(1, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testInsertedInMiddle() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);

		String[] desired = { "red", "green", "pink", "blue" };
		test.match(desired);
		
		assertEquals("Added pink at 2", listener.events.get(0));
		
		assertEquals(1, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testInsertedAtEnd() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);

		String[] desired = { "red", "green", "blue", "pink" };
		test.match(desired);
		
		assertEquals("Added pink at 3", listener.events.get(0));
		
		assertEquals(1, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testRemoveFromBeginning() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);

		String[] desired = { "green", "blue" };
		test.match(desired);
		
		assertEquals("Removed red from 0", listener.events.get(0));
		
		assertEquals(1, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testRemoveFromMiddle() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);
		
		String[] desired = { "red", "blue" };
		test.match(desired);
		
		assertEquals("Removed green from 1", listener.events.get(0));
		
		assertEquals(1, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testRemoveFromEnd() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);
		
		String[] desired = new String[] { "red", "green"};
		test.match(desired);
		
		assertEquals("Removed blue from 2", listener.events.get(0));
		
		assertEquals(1, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testCompletelyDifferent() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);

		String[] desired = { "pink", "orange" };
		test.match(desired);
		
		assertEquals("Added pink at 0", listener.events.get(0));
		assertEquals("Added orange at 1", listener.events.get(1));
		assertEquals("Removed red from 2", listener.events.get(2));
		assertEquals("Removed green from 2", listener.events.get(3));
		assertEquals("Removed blue from 2", listener.events.get(4));
		
		assertEquals(5, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testNothingToStartWith() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);

		String[] desired = { "pink", "orange" };
		test.match(desired);
		
		assertEquals("Added pink at 0", listener.events.get(0));
		assertEquals("Added orange at 1", listener.events.get(1));
		
		assertEquals(2, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}
	
	public void testNothingToEndWith() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		childHelper.insertChild(0, "red");
		childHelper.insertChild(1, "green");
		childHelper.insertChild(2, "blue");
	
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);

		String[] desired = { };
		test.match(desired);
		
		assertEquals("Removed red from 0", listener.events.get(0));
		assertEquals("Removed green from 0", listener.events.get(1));
		assertEquals("Removed blue from 0", listener.events.get(2));
		
		assertEquals(3, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}

	
	public void testNothingAndNothing() {
		
		ChildHelper<String> childHelper = 
			new ChildHelper<String>(new MockStructural());
		
		OurListener listener = new OurListener();
		
		childHelper.addStructuralListener(listener);
		listener.start();
		
		ChildMatch<String> test = new OurChildMatch(childHelper);

		String[] desired = { };
		test.match(desired);
		
		assertEquals(0, listener.events.size());
		
		childHelper.removeStructuralListener(listener);
		
		assertMatches(childHelper, desired);
	}


	private void assertMatches(ChildHelper<String> childHelper, String[] match) {
		
		List<String> children = new ArrayList<String>(
				Arrays.asList(childHelper.getChildren(new String[0])));
		
		class CheckMatch extends ChildMatch<String> {
			
			boolean failed;
			
			public CheckMatch(List<String> children) {
				super(children);
			}
			
			@Override
			protected void insertChild(int index, String child) {
				failed = true;
			}
			
			@Override
			protected void removeChildAt(int index) {
				failed = true;
			}			
		}
		
		CheckMatch check = new CheckMatch(children);
		
		check.match(match);
		
		assertFalse("Match failed.", check.failed);
	}
}
