package org.oddjob.beanbus;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class BeanCopyTest extends TestCase {

	public static class Fruit {

		String fruit;
		int quantity;
		double price;

		public Fruit(String type, int quantity, double price) {
			this.fruit = type;
			this.quantity = quantity;
			this.price = price;
		}
		
		public String getFruit() {
			return fruit;
		}
		public int getQuantity() {
			return quantity;
		}
		public double getPrice() {
			return price;
		}
	
	}

	public void testSimpleCopy() throws ArooaPropertyException, ArooaConversionException {

		List<Fruit> in = Arrays.asList(
				new Fruit("apple", 5, 2.45),
				new Fruit("pear", 2, 1.25));
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/BeanCopyTest.xml", getClass()
						.getClassLoader()));
		oddjob.setExport("iterable", new ArooaObject(in));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> results = lookup.lookup(
				"bus.to.to.collection", List.class);
		
		DynaBean bean1 = (DynaBean) results.get(0);
		assertEquals("apple", bean1.get("snack"));
		assertEquals(5, bean1.get("number"));
		assertEquals(2.45, bean1.get("COST"));
		
		DynaBean bean2 = (DynaBean) results.get(1);
		assertEquals("pear", bean2.get("snack"));
		assertEquals(2, bean2.get("number"));
		assertEquals(1.25, bean2.get("COST"));
		
		assertEquals(2, results.size());
		
		oddjob.destroy();
	}
}
