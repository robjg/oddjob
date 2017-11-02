package org.oddjob.jmx;

import org.junit.Test;

import javax.swing.ImageIcon;

import org.oddjob.OjTestCase;

import org.oddjob.Iconic;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;

public class IconicTest extends OjTestCase {

	class Result implements IconListener {
		IconEvent event;

		public void iconEvent(IconEvent e) {
			this.event = e;
			synchronized (this) {
				notifyAll();
			}
		}
	}
	
	
   @Test
	public void testState() throws ArooaConversionException, InterruptedException {
		
		String xml =
			"<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <rmireg />" +
			"    <jmx:server id='server1'" +
			"            url='service:jmx:rmi://ignored/jndi/rmi://localhost/IconicTest_testState'" +
			"            root='${fruit}' />" +
			" 	 <echo id='fruit'>apples</echo>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));

		oddjob.run();
		
		String address = new OddjobLookup(
				oddjob).lookup("server1.address", String.class);
		
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.setConnection(address);
		
		client.run();
		
		RemoteDirectory remote = client.provideBeanDirectory();
		
		Iconic fruit = (Iconic) remote.lookup("fruit", Iconic.class);
		
		assertNotNull(fruit);
		
		Result result = new Result();

		fruit.addIconListener(result);
		
		String iconId = result.event.getIconId();
		
		assertEquals(IconHelper.COMPLETE, iconId);

		ImageIcon tip = fruit.iconForId(iconId);

		assertEquals("Complete", tip.getDescription());
		
		Resetable resetable = (Resetable) fruit;
		
		resetable.hardReset();

		synchronized (result) {
			result.wait(5000);
		}
		assertEquals(IconHelper.READY, result.event.getIconId());

		client.destroy();
		
		oddjob.destroy();
	}
}
