package org.oddjob.beanbus;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class SimpleBusDriverTest extends TestCase {

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
		
		SimpleBus<Fruit> test = new SimpleBus<Fruit>();
		
		IterableDriver<Apple> driver = new IterableDriver<Apple>();
		driver.setIterable(fruit);
		
		test.setDriver(driver);
		
		Results results = new Results();
		
		driver.setTo(results);
		
		test.run();
		
		assertEquals(2, results.list.size());
	}
}
