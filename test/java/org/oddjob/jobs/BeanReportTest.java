package org.oddjob.jobs;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.reflect.BeanView;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;

public class BeanReportTest extends TestCase {

	public static class Fruit {
		
		private String type;
		
		private String variety;
		
		private String colour;
		
		private double size;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getVariety() {
			return variety;
		}

		public void setVariety(String variety) {
			this.variety = variety;
		}

		public String getColour() {
			return colour;
		}

		public void setColour(String colour) {
			this.colour = colour;
		}

		public double getSize() {
			return size;
		}

		public void setSize(double size) {
			this.size = size;
		}
	}
	
	private class OurView implements BeanView {
		
		@Override
		public String titleFor(String property) {
			if ("colour".equals(property)) {
				return "The Colour";
			}
			return property;
		}

		@Override
		public String[] getProperties() {
			return new String[] {
					"colour", "size", "type", "variety"
			};
		}
	}
	
	
	String EOL = System.getProperty("line.separator");

	private Object[] createFruit() {
		
		Fruit fruit1 = new Fruit();
		fruit1.setType("Apple");
		fruit1.setVariety("Cox");
		fruit1.setColour("Red and Green");
		fruit1.setSize(7.6);
		
		Fruit fruit2 = new Fruit();
		fruit2.setType("Orange");
		fruit2.setVariety("Jaffa");
		fruit2.setColour("Orange");
		fruit2.setSize(9.245);

		return new Object[] { fruit1, fruit2 };
	}
	
	
	public void testFruitReport() {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Object[] values = createFruit(); 
		
		BeanReportJob test = new BeanReportJob();
		test.setOutput(out);
		test.setArooaSession(new StandardArooaSession());
		test.setBeans(Arrays.asList(values));
		test.setBeanView(new OurView());
		
		test.run();

		String expected = 
			"The Colour     size   type    variety" + EOL + 
			"-------------  -----  ------  -------" + EOL +
			"Red and Green  7.6    Apple   Cox" + EOL +
			"Orange         9.245  Orange  Jaffa" + EOL;
		
		assertEquals(expected, out.toString());
	}
	
	public void testInOddjob() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/BeanReportTest.xml",
				getClass().getClassLoader()));

		oddjob.run();
		
		String expected = 			
			"TYPE    VARIETY  COLOUR         SIZE" + EOL + 
			"------  -------  -------------  -----" + EOL +
			"Apple   Cox      Red And Green  7.6" + EOL +
			"Orange  Jaffa    Orange         9.245" + EOL;
		
		String results = new OddjobLookup(oddjob).lookup(
				"results-buffer", String.class); 

		assertEquals(expected, results);
		
		oddjob.destroy();
	}
	
}
