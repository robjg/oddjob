package org.oddjob.framework;

import junit.framework.TestCase;

public class ProxyGeneratorTest extends TestCase {

	// The component implements this.
	interface Fruit {
		
		String getType();
	}
	
	class Apple implements Fruit {

		@Override
		public String getType() {
			return "apple";
		}
	}
	
	// We proxy this.
	interface Snack {
		public void eat();
	}
	
	// Invocations for Snack
	class FruitWrapper implements ComponentWrapper, Snack {
		
		final Fruit fruit;

		int eaten;
		String type;
		
		public FruitWrapper(Fruit fruit) {
			this.fruit = fruit;
		}
		
		@Override
		public void eat() {
			++eaten;
			type = fruit.getType();
		}
	}
	
	public void testGenerate() {
		
		ProxyGenerator<Fruit> test = new ProxyGenerator<Fruit>();
		
		final Fruit fruit = new Apple();
		
		final FruitWrapper wrapper = new FruitWrapper(fruit);
		
		Object proxy = test.generate(fruit, 
				new WrapperFactory<ProxyGeneratorTest.Fruit>() {
					
					@Override
					public Class<?>[] wrappingInterfacesFor(Fruit wrapped) {
						return new Class[] { Snack.class };
					}
					
					@Override
					public ComponentWrapper wrapperFor(Fruit wrapped, 
							Object proxy) {
						return wrapper;
					}
				}, getClass().getClassLoader());
		
		assertTrue(proxy instanceof Fruit);
		assertTrue(proxy instanceof Snack);
		
		assertEquals("apple", ((Fruit) proxy).getType());
		
		((Snack) proxy).eat();
		
		assertEquals(1, wrapper.eaten);
		assertEquals("apple", wrapper.type);
	}
	
	interface Vegetable {
		String getColour();
	}
	
	class Tomato implements Vegetable {
		
		@Override
		public String getColour() {
			return "red";
		}
	}
	
	class VegetableAdaptor implements Fruit, Adaptor {
		final Vegetable vegetable;
		
		public VegetableAdaptor(Vegetable vegetable) {
			this.vegetable = vegetable;
		}
		
		@Override
		public String getType() {
			return vegetable.getClass().getSimpleName();
		}
		
		@Override
		public Object getComponent() {
			return vegetable;
		}
	}
	
	
	public void testGenerateAdaptor() {
		
		ProxyGenerator<Fruit> test = new ProxyGenerator<Fruit>();
		
		final Fruit fruit = new VegetableAdaptor(new Tomato());
		
		final FruitWrapper wrapper = new FruitWrapper(fruit);
		
		Object proxy = test.generate(fruit, 
				new WrapperFactory<ProxyGeneratorTest.Fruit>() {
					
					@Override
					public Class<?>[] wrappingInterfacesFor(Fruit wrapped) {
						return new Class[] { Snack.class };
					}
					
					@Override
					public ComponentWrapper wrapperFor(Fruit wrapped, 
							Object proxy) {
						return wrapper;
					}
				}, getClass().getClassLoader());
		
		assertTrue(proxy instanceof Vegetable);
		assertTrue(proxy instanceof Snack);
		
		assertEquals("red", ((Vegetable) proxy).getColour());
		
		((Snack) proxy).eat();
		
		assertEquals(1, wrapper.eaten);
		assertEquals("Tomato", wrapper.type);
	}
}
