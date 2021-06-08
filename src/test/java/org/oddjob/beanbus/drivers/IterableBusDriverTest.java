package org.oddjob.beanbus.drivers;

import org.junit.Test;
import org.oddjob.OjTestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class IterableBusDriverTest extends OjTestCase {

	private interface Food {
		
	}
	
	private interface Fruit extends Food {
		
	}
	
	private class Apple implements Fruit {
		
	}
		
   @Test
	public void testSimpleRun() {
		
		List<Apple> fruit = new ArrayList<Apple>();
		
		fruit.add(new Apple());
		fruit.add(new Apple());
		
		IterableBusDriver<Apple> test = new IterableBusDriver<Apple>();
		test.setBeans(fruit);
		
		List<Food> results = new ArrayList<Food>();
		
		test.setTo(results::add);
		
		test.run();
		
		assertEquals(2, results.size());
	}
	
	private class BlockingIterable implements Iterable<String> {
		
		CountDownLatch countdown = new CountDownLatch(1);

		boolean interrupted;
		
		@Override
		public Iterator<String> iterator() {
			return new Iterator<String>() {
				
				@Override
				public boolean hasNext() {
					try {
						synchronized (IterableBusDriverTest.this) {
							countdown.countDown();
							IterableBusDriverTest.this.wait();
							fail("Shouldn't happen");
						}
					} catch (InterruptedException e) {
						interrupted = true;
					}
					return false;
				}
				
				@Override
				public String next() {
					throw new RuntimeException("Unexpected!");
				}
				
				@Override
				public void remove() {
					throw new RuntimeException("Unexpected!");
				}
			};
		}
	}
	
   @Test
	public void testBlockedIterator() throws InterruptedException {
		
		BlockingIterable beans = new BlockingIterable();
		IterableBusDriver<String> test = new IterableBusDriver<>();
		test.setBeans(beans);
		
		Thread t = new Thread(test);
		t.start();
		beans.countdown.await();
		
		synchronized (IterableBusDriverTest.this) {
			// Iterator must now be blocking
		}
		
		test.stop();
		
		t.join();
		
		assertEquals(true, beans.interrupted);
	}
}
