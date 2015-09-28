package org.oddjob.beanbus.destinations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;

public class BeanDiagnosticsTest extends TestCase {

	private static final Logger logger = Logger.getLogger(BeanDiagnosticsTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.info("--------------------------------  " + getName() + 
				"  -----------------------------");
	}
	
	public static class Fruit {
		
		public String getType() {
			return null;
		}
		
		public int getQuantity() {
			return 0;
		}
		
		public void setQuantity(int quantity) {
		}
		
		public String getParam(String key) {
			return null;
		}
		
		public void setThing(int index, Double thing) {
			
		}
	}
	
	String LS = OddjobTestHelper.LS;
	
	public void testPrintTypeInfoTest() {
		
		ArooaSession session = new StandardArooaSession();
		
		BeanDiagnostics<Object> test = new BeanDiagnostics<Object>();
		test.setArooaSession(session);
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(buffer);
		
		PropertyAccessor accessor = session.getTools().getPropertyAccessor();
				
		test.printTypeInfo(accessor.getClassName(new Fruit()), out);
		
		out.close();
		
		String expected = 
				"Type: SimpleArooaClass: class org.oddjob.beanbus.destinations.BeanDiagnosticsTest$Fruit" + LS +
				" Properties:" + LS +
				"  class: java.lang.Class (Read Only)" + LS +
				"  param: java.lang.String, mapped (Read Only)" + LS +
				"  quantity: int" + LS +
				"  thing: java.lang.Double, indexed (Write Only)" + LS +
				"  type: java.lang.String (Read Only)" + LS;
		
		assertEquals(expected, new String(buffer.toByteArray()));
	}
	
	public void testInOddjob() {
		
		File config = new File(getClass().getResource(
				"BeanDiagnostics.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(config);
		
		ConsoleCapture console = new ConsoleCapture();
		console.captureConsole();
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		 
		assertEquals("Analysed 3 beans. Discovered 2 types.",
				lines[0].trim());
		assertEquals("Type: DynaArooaClass: dynaClassName=org.oddjob.arooa.beanutils.MagicBean:OurMagicClass, forClass=interface org.apache.commons.beanutils.DynaBean",
				lines[1].trim());
		assertEquals("Properties:",
				lines[2].trim());
		assertEquals("price: java.lang.Double",
				lines[3].trim());
		assertEquals("vegtable: java.lang.String",
				lines[4].trim());
		assertEquals("Type: SimpleArooaClass: class org.oddjob.beanbus.destinations.BeanDiagnosticsTest$Fruit",
				lines[5].trim());
		assertEquals("Properties:",
				lines[6].trim());
		assertEquals("class: java.lang.Class (Read Only)",
				lines[7].trim());
		assertEquals("param: java.lang.String, mapped (Read Only)",
				lines[8].trim());
		assertEquals("quantity: int",
				lines[9].trim());
		assertEquals("thing: java.lang.Double, indexed (Write Only)",
				lines[10].trim());
		assertEquals("type: java.lang.String (Read Only)",
				lines[11].trim());

	}
}
