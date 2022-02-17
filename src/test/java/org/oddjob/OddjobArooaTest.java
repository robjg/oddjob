package org.oddjob;

import org.junit.Test;
import org.oddjob.arooa.*;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.parsing.ParseContext;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.registry.SimpleComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.ParentState;

/**
 * Test the Arooa aspects of Oddjob.
 *  
 * @author rob
 *
 */
public class OddjobArooaTest extends OjTestCase {

   @Test
	public void testInnerJobDescriptor() throws ArooaParseException {

		OddjobServices services = new MockOddjobServices() {
			@Override
			public ClassLoader getClassLoader() {
				return getClass().getClassLoader();
			}
		};
		
		Oddjob.OddjobRoot rootOddjob = new Oddjob().new OddjobRoot(
				services);
		
		StandardArooaParser parser = new StandardArooaParser(rootOddjob);
		
		parser.parse(new ArooaConfiguration() {
			@Override
			public <P extends ParseContext<P>> ConfigurationHandle<P> parse(P parentContext)
					throws ArooaParseException {
				ArooaElement element = new ArooaElement("watever");
				element = element.addAttribute("id", "oddjob");
				parentContext.getElementHandler().onStartElement(element, parentContext);
				
				return new MockConfigurationHandle();
			}
		});
		
		ArooaSession session = parser.getSession();
		
		ArooaDescriptor descriptor = session.getArooaDescriptor();
		
		ArooaBeanDescriptor bd = descriptor.getBeanDescriptor(
				new SimpleArooaClass(Oddjob.OddjobRoot.class),
				session.getTools().getPropertyAccessor());
		
		assertNotNull(bd);
		
		assertEquals("job", bd.getComponentProperty());
		assertNull(bd.getParsingInterceptor());
	}
	
	public static class SessionCapture implements ArooaSessionAware {
		
		ArooaSession session;
		
		public void setArooaSession(ArooaSession session) {
			this.session = session;
		}

		public ArooaSession getSession() {
			return session;
		}
	}
	
   @Test
	public void testInnerSession() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean class='" + SessionCapture.class.getName() + "' id='x'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		SessionCapture sc = (SessionCapture) new OddjobLookup(oddjob).lookup("x");
		
		ArooaDescriptor descriptor = sc.session.getArooaDescriptor();
				
		assertEquals(new SimpleArooaClass(Oddjob.class), 
				descriptor.getElementMappings().mappingFor(
						new ArooaElement("oddjob"),
						new InstantiationContext(ArooaType.COMPONENT, null)));
		
	}
	
	private class OurSession extends StandardArooaSession {
		
		SimpleComponentPool componentPool = new SimpleComponentPool();
		
		BeanRegistry beanRegistry = new SimpleBeanRegistry();
		
		@Override
		public ComponentPool getComponentPool() {
			return componentPool;
		}
		
		@Override
		public BeanRegistry getBeanRegistry() {
			return beanRegistry;
		}
		
	}
	
	private class OurContext extends MockArooaContext {
		
		ArooaSession session;
		
		public OurContext(ArooaSession session) {
			this.session = session;
		}
		
		@Override
		public RuntimeConfiguration getRuntime() {
			return new MockRuntimeConfiguration() {
				@Override
				public void configure() {
				}
			};
		} 
		
		@Override
		public ArooaSession getSession() {
			return session;
		}
	}
	
   @Test
	public void testHierarchicalRegistry() {
		
		OurSession existingSession = new OurSession();
		
		String xml = "<oddjob id='x'/>";
				
		Oddjob oddjob = new Oddjob();
		oddjob.setArooaSession(existingSession);
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		ComponentPool pool = existingSession.getComponentPool();
		
		pool.registerComponent(
				new ComponentTrinity(oddjob, oddjob, new OurContext(existingSession)), "y");
		
		oddjob.run();
		
		Object result = existingSession.getBeanRegistry().lookup("y/x");

		assertNotNull(result);
	}
	
	public static class MyEcho extends SimpleJob {
		@Override
		protected int execute() throws Throwable {
			return 0;
		}
		
	}

	String xml = 
		"<oddjob xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
		" <job>" +
        "  <oddjob id='x'>" +
		"   <descriptorFactory>" +
		"    <arooa:descriptor>" +
		"     <components>" +
        "        <arooa:bean-def className='" + MyEcho.class.getName() + "'" +
        "                        element='echo'/>" +
        "     </components>" +
        "    </arooa:descriptor>" +
        "   </descriptorFactory>" +
        "   <configuration>" +
        "    <arooa:configuration>" +
        "     <xml>" +
        "      <xml>" +
        "       <oddjob>" +
        "        <job>" +
        "         <echo id='y'/>" +
        "        </job>" +
        "       </oddjob>" +
        "      </xml>" +
        "     </xml>" +
        "    </arooa:configuration>" +
        "   </configuration>" +
        "  </oddjob>" +
        " </job>" +
        "</oddjob>";
	
	
   @Test
	public void testDescriptorOverride() {

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();
		
		Object echo = new OddjobLookup(oddjob).lookup("x/y");
		
		assertEquals(MyEcho.class, echo.getClass());
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		oddjob.destroy();

		assertEquals(ParentState.DESTROYED, 
				oddjob.lastStateEvent().getState());
	}

   @Test
	public void testNestedDescriptorOverride() throws ArooaParseException {

		// add another layer of Oddjob.
		String moreXml = 
			"<oddjob xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
			" <job>" +
			"  <oddjob id='z'>" +
			"   <configuration>" +
			"    <arooa:configuration>" +
			"     <xml>" +
			"      <xml>" +
			xml +
			"      </xml>" +
			"     </xml>" +
			"    </arooa:configuration>" +
			"   </configuration>" +
			"  </oddjob>" +
            " </job>" +
            "</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", moreXml));
		oddjob.run();
		
		Object echo = new OddjobLookup(oddjob).lookup("z/x/y");
		
		assertEquals(MyEcho.class, echo.getClass());
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		oddjob.destroy();

		assertEquals(ParentState.DESTROYED, 
				oddjob.lastStateEvent().getState());
	}

}
