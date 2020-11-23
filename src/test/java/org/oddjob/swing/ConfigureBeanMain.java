package org.oddjob.swing;

import org.oddjob.Oddjob;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.persist.MapPersister;

import java.util.Date;

public class ConfigureBeanMain {

	public static class MyBean {
		
		private String name;
		
		private Date dateOfBirth;
		
		private double height;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Date getDateOfBirth() {
			return dateOfBirth;
		}

		public void setDateOfBirth(Date dateOfBirth) {
			this.dateOfBirth = dateOfBirth;
		}

		public double getHeight() {
			return height;
		}

		public void setHeight(double height) {
			this.height = height;
		}
	}
	
	public void simpleBeanOnly() {
		ConfigureBeanJob job = new ConfigureBeanJob();
		
		MyBean bean = new MyBean();
		
		job.setBean(bean);
		job.setArooaSession(new StandardArooaSession(
				new OddjobDescriptorFactory().createDescriptor(
						getClass().getClassLoader())));
		
		job.run();
		
		System.out.println(bean.getName());
		System.out.println(bean.getDateOfBirth());
		System.out.println(bean.getHeight());
	}
	
	public void simpleInOddjob() throws ArooaPropertyException, ArooaConversionException {
		
		String inner = 
			"<oddjob xmlns:magic='http://rgordon.co.uk/oddjob/magic'>" +
			" <job>" +
			"  <class class='org.oddjob.swing.ConfigureBeanJob'" +
			"         id='job'>" +
			"   <bean>" +
			"	 <magic:my-bean id='my-bean'/>" +
			"   </bean>" +
			"  </class>" +
			" </job>" +
			"</oddjob>";

		String outer = 
			"<oddjob xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
			" <job>" +
			"  <oddjob id='inner' name='Inner'>" +
			"   <descriptorFactory>" +
			"    <arooa:magic-beans>" +
			"     <definitions>" +
			"      <arooa:magic-bean name='my-bean'>" +
			"       <properties>" +
			"        <arooa:magic-property name='name'/>" +
			"        <arooa:magic-property name='dateOfBirth'" +
			"                     type='java.util.Date'/>" +
			"        <arooa:magic-property name='height'" +
			"                     type='java.lang.Double'/>" +
			"       </properties>" +
			"      </arooa:magic-bean>" +
			"     </definitions>" +
			"    </arooa:magic-beans>" +
			"   </descriptorFactory>" +
			"   <configuration>" +
			"    <arooa:configuration>" +
			"     <xml>" +
			"      <xml>" +
			inner +
			"      </xml>" +
			"     </xml>" +
			"    </arooa:configuration>" +
			"   </configuration>" +
			"  </oddjob>" +
			" </job>" +
			"</oddjob>";
		
		MapPersister persister = new MapPersister();
		persister.setPath("test");
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", outer));
		oddjob.setPersister(persister);
		oddjob.run();

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		System.out.println(lookup.lookup("inner/my-bean.name"));
		System.out.println(lookup.lookup("inner/my-bean.dateOfBirth"));
		System.out.println(lookup.lookup("inner/my-bean.height"));
		
		oddjob.destroy();
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setConfiguration(new XMLConfiguration("XML", outer));
		oddjob2.setPersister(persister);
		oddjob2.setName("Outer");
		oddjob2.load();

		OddjobLookup lookup2 = new OddjobLookup(oddjob2);
		
		Oddjob innerOddjob = lookup2.lookup("inner", Oddjob.class);
		innerOddjob.load();
		
		Object resetable = lookup2.lookup("inner/job");

		((Resettable) resetable).hardReset();
		
		((Runnable) resetable).run();
		
		System.out.println(lookup2.lookup("inner/my-bean.name"));
		System.out.println(lookup2.lookup("inner/my-bean.dateOfBirth"));
		System.out.println(lookup2.lookup("inner/my-bean.height"));
	}
	
	public static void main(String... args) throws ArooaParseException, ArooaPropertyException, ArooaConversionException {
		
//		new ConfigureBeanMain().simpleBeanOnly();
		new ConfigureBeanMain().simpleInOddjob();
	}
	
}
