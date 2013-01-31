package org.oddjob.beanbus;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class IterableBusDriverTest extends TestCase {

	private interface Food {
		
	}
	
	private interface Fruit extends Food {
		
	}
	
	private class Apple implements Fruit {
		
	}
	
	private class Results implements Destination<Food> {
		List<Food> list = new ArrayList<Food>();

		public void accept(Food bean) {
			list.add(bean);
		};
	}
	
	public void testSimpleRun() {
		
		List<Apple> fruit = new ArrayList<Apple>();
		
		fruit.add(new Apple());
		fruit.add(new Apple());
		
		IterableBusDriver<Apple> test = new IterableBusDriver<Apple>();
		test.setBeans(fruit);
		
		Results results = new Results();
		
		test.setTo(results);
		
		test.run();
		
		assertEquals(2, results.list.size());
	}
}
