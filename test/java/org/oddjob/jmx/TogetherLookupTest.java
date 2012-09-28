package org.oddjob.jmx;

import java.net.MalformedURLException;

import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;

public class TogetherLookupTest extends TestCase {
	
	public void testSameRegistry() throws ArooaConversionException {
		
		String xml =
			"<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <rmireg />" +
			"    <jmx:server id='server1'" +
			"            url='service:jmx:rmi://ignored/jndi/rmi://localhost/server1'" +
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
		
		assertEquals("/jndi/rmi://localhost/server1",
				remote.getServerId().toString());

		Object fruit = remote.lookup("fruit");
		assertNotNull(fruit);
		
		RemoteRegistryCrawler crawler = new RemoteRegistryCrawler(remote);

		assertEquals(new Address(remote.getServerId(), new Path("fruit")),
				crawler.addressFor(fruit));		
		
		client.destroy();
		
		oddjob.destroy();
	}
	
	public void testDifferentRegistrySameServer() throws ArooaConversionException {
		
		String xml =
			"<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <rmireg />" +
			"    <jmx:server id='server1'" +
			"            url='service:jmx:rmi://ignored/jndi/rmi://localhost/server1'" +
			"            root='${fruit}' />" +
			"    <oddjob id='fruit'>" +
			"     <configuration>" +
			"        <xml>" +
			"         <oddjob>" +
			"          <job>" +   
			" 	        <echo id='apples'>apples</echo>" +
			"          </job>" +
			"         </oddjob>" +
			"        </xml>" +
			"     </configuration>" +
			"    </oddjob>" +
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
		
		Object apples = remote.lookup("fruit/apples");
		assertNotNull(apples);
		
		RemoteRegistryCrawler crawler = new RemoteRegistryCrawler(remote);

		assertEquals(new Address(remote.getServerId(), new Path("fruit/apples")),
				crawler.addressFor(apples));		
		
		client.destroy();
		
		oddjob.destroy();
	}
	
	public void testDifferentServer() throws ArooaConversionException, MalformedURLException {
		
		String xml2 = 
			"<oddjob id='this' xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <rmireg />" +
			"    <jmx:server id='server2'" +
			"            url='service:jmx:rmi://ignored/jndi/rmi://localhost/server2'" +
			"            root='${apples}' />" +
			" 	 <echo id='apples'>apples</echo>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";

		Oddjob oddjob2 = new Oddjob();
		oddjob2.setConfiguration(new XMLConfiguration("XML", xml2));
		
		oddjob2.run();
		
		String xml1 = 
			"<oddjob id='this' xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <rmireg />" +
			"    <jmx:server id='server1'" +
			"            url='service:jmx:rmi://ignored/jndi/rmi://localhost/server1'" +
			"            root='${fruit}' />" +
			"    <jmx:client id='fruit' connection='${this.args[0]}' />" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";

		String address2 = new OddjobLookup(oddjob2).lookup(
				"server2.address", String.class);
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setConfiguration(new XMLConfiguration("XML", xml1));
		oddjob1.setArgs(new String[] { address2 } );

		oddjob1.run();
			
		String address1 = new OddjobLookup(
				oddjob1).lookup("server1.address", String.class);
		
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.setConnection(address1);
		
		client.run();
		
		RemoteDirectory remote = client.provideBeanDirectory();
		
		Object apples = remote.lookup("fruit/apples");
		assertNotNull(apples);
		
		RemoteRegistryCrawler crawler = new RemoteRegistryCrawler(remote);

		JMXServiceURL url = new JMXServiceURL(address2);
		
		
		assertEquals(new Address(new ServerId(url.getURLPath()), new Path("apples")).toString(),
				crawler.addressFor(apples).toString());		
		
		client.destroy();
		
		oddjob1.destroy();
		
		oddjob2.destroy();
	}
}
