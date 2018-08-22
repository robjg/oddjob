/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.structural;
import org.junit.Before;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.MockStructural;


/**
 * 
 */
public class ChildHelperTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(ChildHelperTest.class);
	
    @Before
    public void setUp() throws Exception {
		logger.debug("------------   " + getName() + "  -------------");
	}
	
   @Test
	public void testReplaceChild() {
		class MyL implements StructuralListener {
			int added;
			int removed;
			public void childAdded(StructuralEvent event) {
				added = event.getIndex();
			}
			public void childRemoved(StructuralEvent event) {
				removed = event.getIndex();
			}
			
		}

		Object o1 = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		
		ChildHelper<Object> test = new ChildHelper<Object>(
				new MockStructural());
		
		MyL l = new MyL();
		test.addStructuralListener(l);
		
		test.insertChild(0, o1);
		test.insertChild(1, o2);
		
		test.removeChildAt(0);
		test.insertChild(0, o3);
		
		assertEquals("replace and in right place.", o3, test.getChildren()[0]);
		assertEquals("notify correct removed index.", 0, l.removed);
		assertEquals("notify correct added index.", 0, l.added);
	}
	
	
   @Test
	public void testDeadlockOnNotify() throws InterruptedException {

		final ExecutorService executor = Executors.newFixedThreadPool(5);
		
		final ChildHelper<Object> test = new ChildHelper<Object>(new MockStructural());
		test.insertChild(0, new Object());
		test.insertChild(0, new Object());
		test.insertChild(0, new Object());
		test.insertChild(0, new Object());
		test.insertChild(0, new Object());
		
		class LoopbackListener implements StructuralListener {
			
			ChildHelper<Object> copies = new ChildHelper<Object>(new MockStructural());
			
			AtomicInteger events = new AtomicInteger();
			
			CountDownLatch finished = new CountDownLatch(74);
			
			public void childAdded(StructuralEvent event) {
				copies.insertChild(event.getIndex(), event.getChild());
				onEvent(event);
			}
			
			public void childRemoved(StructuralEvent event) {
				copies.removeChildAt(event.getIndex());
				onEvent(event);
			}
			
			private void onEvent(StructuralEvent event) {				
				int events = this.events.incrementAndGet();

				logger.debug("" + events);
				
				if (events < 50) {
					executor.submit(new Runnable() {
						public void run() {
							logger.debug("Inserting.");
							test.insertChild(0, new Object());
						}
					});
				}
				else if (events < 70) {
					executor.submit(new Runnable() {
						public void run() {
							logger.debug("Removing.");
							test.removeChildAt(0);
						}
					});
				}
				finished.countDown();
			}
			
		}
		
		LoopbackListener listener = new LoopbackListener();
		
		logger.debug("Starting");
		
		test.addStructuralListener(listener);
		
		logger.debug("Waiting");
		
		listener.finished.await();
		
		assertEquals(34, listener.copies.size());
		
		logger.debug("Shutdown.");
		
		executor.shutdown();
	}
	
	private class StructureAlteringListener implements StructuralListener {
	
		ChildHelper<String> test;
		
		List<String> children = new ArrayList<String>();
		
		@Override
		public void childAdded(StructuralEvent event) {
			children.add(event.getIndex(), (String) event.getChild());
			if (children.size() == 1) {
				test.insertChild(1, "orange");
			}
			if (children.size() == 2) {
				test.removeChildAt(0);
			}
		}
		
		@Override
		public void childRemoved(StructuralEvent event) {
			children.remove(event.getIndex());
		}
	}
	
   @Test
	public void testNoMissedEvent() {
				
		ChildHelper<String> test = new ChildHelper<String>(new MockStructural());
		test.insertChild(0, "apple");
		
		StructureAlteringListener listener = new StructureAlteringListener();
		listener.test = test;
		test.addStructuralListener(listener);
		
		assertEquals(1, listener.children.size());
		assertEquals("orange", listener.children.get(0));		
	}
	
	private enum Type { ADDED, REMOVED }
	
	private class SimpleListener implements StructuralListener {
		
		private List<Type> types = new ArrayList<Type>();
		private List<StructuralEvent> events = new ArrayList<StructuralEvent>();
		
		@Override
		public void childAdded(StructuralEvent event) {
			types.add(Type.ADDED);
			events.add(event);
		}
		
		@Override
		public void childRemoved(StructuralEvent event) {
			types.add(Type.REMOVED);
			events.add(event);
		}
	}
	
   @Test
	public void testAddAndRemove() {
		
		ChildHelper<Object> test = new ChildHelper<Object>(new MockStructural());
				
		SimpleListener listener = new SimpleListener();
		
		test.addStructuralListener(listener);
		
		Object o1 = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		Object o4 = new Object();
				
		test.addChild(o1);
		
		assertEquals(1, listener.types.size());
		assertEquals(Type.ADDED, listener.types.get(0));
		assertEquals(o1, listener.events.get(0).getChild());
		assertEquals(0, listener.events.get(0).getIndex());
		
		test.addChild(o2);
		
		assertEquals(2, listener.types.size());
		assertEquals(Type.ADDED, listener.types.get(1));
		assertEquals(o2, listener.events.get(1).getChild());
		assertEquals(1, listener.events.get(1).getIndex());
		
		test.addChild(o3);
		
		assertEquals(3, listener.types.size());
		assertEquals(Type.ADDED, listener.types.get(2));
		assertEquals(o3, listener.events.get(2).getChild());
		assertEquals(2, listener.events.get(2).getIndex());
		
		test.removeChild(o2);
		
		assertEquals(4, listener.types.size());
		assertEquals(Type.REMOVED, listener.types.get(3));
		assertEquals(o2, listener.events.get(3).getChild());
		assertEquals(1, listener.events.get(3).getIndex());
		
		test.removeChild(o1);
		
		assertEquals(5, listener.types.size());
		assertEquals(Type.REMOVED, listener.types.get(4));
		assertEquals(o1, listener.events.get(4).getChild());
		assertEquals(0, listener.events.get(4).getIndex());
		
		test.addChild(o4);
		
		assertEquals(6, listener.types.size());
		assertEquals(Type.ADDED, listener.types.get(5));
		assertEquals(o4, listener.events.get(5).getChild());
		assertEquals(1, listener.events.get(5).getIndex());
		
		test.removeChild(o3);
		
		assertEquals(7, listener.types.size());
		assertEquals(Type.REMOVED, listener.types.get(6));
		assertEquals(o3, listener.events.get(6).getChild());
		assertEquals(0, listener.events.get(6).getIndex());
		
		try {
			test.removeChild(o1);
			fail("Child doesn't exist");
		}
		catch (IllegalStateException e) {
			// expected.
		}
		
		assertEquals(7, listener.types.size());
	}
	
   @Test
	public void testIterable() {
		
		ChildHelper<String> test = new ChildHelper<String>(new MockStructural());
		test.insertChild(0, "apple");
		test.insertChild(1, "orange");
		test.insertChild(2, "pear");
		
		Iterator<String> iterator = test.iterator();
		
		assertEquals(true, iterator.hasNext());
		assertEquals("apple", iterator.next());
		
		assertEquals(true, iterator.hasNext());
		assertEquals("orange", iterator.next());
		
		test.removeChild("apple");
		test.insertChild(2, "banana");
		
		assertEquals(true, iterator.hasNext());
		assertEquals("pear", iterator.next());
		
		assertEquals(true, iterator.hasNext());
		assertEquals("banana", iterator.next());
		
		test.removeChild("banana");
		test.insertChild(2, "kiwi");
		
		assertEquals(true, iterator.hasNext());
		assertEquals("kiwi", iterator.next());
		
		assertEquals(false, iterator.hasNext());
	}
	
   @Test
	public void testIterableInFor() {
		
		ChildHelper<String> test = new ChildHelper<String>(new MockStructural());
		test.insertChild(0, "apple");
		test.insertChild(1, "orange");
		test.insertChild(2, "pear");
		
		List<String> results = new ArrayList<String>();
		
		for (String next : test) {
			results.add(next);
		}
		
		assertEquals("apple", results.get(0));
		assertEquals("orange", results.get(1));
		assertEquals("pear", results.get(2));
		assertEquals(3, results.size());
	}

}
