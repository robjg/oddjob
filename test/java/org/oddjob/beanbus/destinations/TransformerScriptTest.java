package org.oddjob.beanbus.destinations;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.beanutils.MagicBeanClassCreator;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class TransformerScriptTest extends TestCase {

	public void testSimpleMagicBeanTransform() throws ScriptException {
		
		MagicBeanClassCreator creator = new MagicBeanClassCreator("Test");
		creator.addProperty("fruit", String.class);
		creator.addProperty("quantity", Integer.class);
		ArooaClass arooaClass = creator.create();
		
		PropertyAccessor accessor = new BeanUtilsPropertyAccessor();
		
		Object bean1 = arooaClass.newInstance();
		accessor.setProperty(bean1, "fruit", "apple");
		accessor.setProperty(bean1, "quantity", 42);
		
		Object bean2 = arooaClass.newInstance();
		accessor.setProperty(bean2, "fruit", "orange");
		accessor.setProperty(bean2, "quantity", 2);
		
		TransformerScript<Object, Object> test = 
				new TransformerScript<Object, Object>();
		
		test.setScript("function transform(from) {" +
				" if (from.get('quantity') > 24) {" +
				"  return null;" +
				" }" +
				" else {" +
				"  return from" +
				" }}");
		
		List<Object> results = new ArrayList<Object>();
		
		test.setTo(results);
		test.configured();
		
		test.add(bean1);
		test.add(bean2);
		
		Assert.assertEquals(1,  results.size());
	}
	
	public static class Fruit {
		
		private String type;
		
		private int quantity;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/destinations/TransformerScriptExample.xml", getClass()
						.getClassLoader()));
		
		StateSteps states = new StateSteps(oddjob);
		states.startCheck(ParentState.READY,
				ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		oddjob.run();
		
		states.checkNow();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<Fruit> results = lookup.lookup(
				"results.beans", List.class);
		
		assertEquals("Banana", results.get(0).getType());
		assertEquals("Pear", results.get(1).getType());
		
		assertEquals(2, results.size());
		
		Object beanBus = lookup.lookup("bean-bus");
		
		((Resetable) beanBus).hardReset();
		((Runnable) beanBus).run();
		
		results = lookup.lookup(
				"results.beans", List.class);
		
		assertEquals("Banana", results.get(0).getType());
		assertEquals("Pear", results.get(1).getType());
		
		assertEquals(2, results.size());
		
		oddjob.destroy();
	}
}
