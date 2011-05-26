/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.structural;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.MockStructural;


/**
 * 
 */
public class ChildHelperTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ChildHelperTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("------------   " + getName() + "  -------------");
	}
	
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
	
	class StructureAlteringListener implements StructuralListener {
	
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
	
	public void testNoMissedEvent() {
				
		ChildHelper<String> test = new ChildHelper<String>(new MockStructural());
		test.insertChild(0, "apple");
		
		StructureAlteringListener listener = new StructureAlteringListener();
		listener.test = test;
		test.addStructuralListener(listener);
		
		assertEquals(1, listener.children.size());
		assertEquals("orange", listener.children.get(0));		
	}
}
