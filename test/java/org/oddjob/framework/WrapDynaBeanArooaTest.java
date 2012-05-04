package org.oddjob.framework;

import java.net.URL;

import junit.framework.TestCase;

import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaParser;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;

public class WrapDynaBeanArooaTest extends TestCase {

	public static class Root {
		
		@ArooaComponent
		public void setStuff(Object stuff) {
			
		}
	}
	
	public static class Stuff {
		
		private String text;
		
		private URL url;
		
		public URL getUrl() {
			return url;
		}

		public void setUrl(URL url) {
			this.url = url;
		}

		public void setText(String text) {
			this.text = text;
		};
		
		public String getText() {
			return text;
		}
	}
	
	private class OurSession extends StandardArooaSession {
		
		@Override
		public ComponentProxyResolver getComponentProxyResolver() {
			return new ComponentProxyResolver() {
				@Override
				public Object resolve(Object object, ArooaSession session) {
					return new WrapDynaBean(object);
				}
				@Override
				public Object restore(Object proxy, ArooaSession session) {
					throw new RuntimeException("Unexpected.");
				}
			};
		}
	}
	
	public void testInArooa() throws ArooaParseException, ArooaPropertyException, ArooaConversionException {
		
		String xml = 
			"<root>" +
			" <stuff>" +
			"  <bean id='s' class='" + Stuff.class.getName() + "' text='Hello'>" +
			"   <url>" +
			"    <value value='file:/stuff'/>" +
			"   </url>" +
			"  </bean>" +
			" </stuff>" +
			"</root>";
		
		ArooaSession session = new OurSession();
		
		Root root = new Root();
		
		ArooaParser parser = new StandardArooaParser(root, session);
		
		parser.parse(new XMLConfiguration("TEST", xml));
				
		BeanDirectory lookup = session.getBeanRegistry();
		
		Object stuff = lookup.lookup("s");
		
		session.getComponentPool().configure(stuff);
		
		assertEquals(WrapDynaBean.class, stuff.getClass());
		
		PropertyAccessor accessor = session.getTools().getPropertyAccessor();

		ArooaClass arooaClass = accessor.getClassName(stuff);
		
		assertEquals(WrapDynaArooaClass.class, arooaClass.getClass());
		
		BeanOverview overview = arooaClass.getBeanOverview(
				accessor);
		
		assertEquals(WrapDynaBeanOverview.class, overview.getClass());
		assertTrue(overview.hasReadableProperty("url"));
		
		String text = lookup.lookup("s.text", String.class);
		
		assertEquals("Hello", text);
		
		String url = lookup.lookup("s.url", String.class);
		
		assertEquals("file:/stuff", url);
	}
}
