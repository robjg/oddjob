package org.oddjob.swing;

import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.DesignSeedContext;
import org.oddjob.arooa.design.GenericDesignFactory;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.design.view.ValueDialog;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;

import java.awt.*;
import java.io.Serializable;
import java.util.concurrent.Callable;

public class ConfigureBeanJob implements Serializable, Runnable, ArooaSessionAware {
	private static final long serialVersionUID = 2010030100L;
	
	private transient Object bean;

	private transient ArooaSession session;

	private String beanConfig;
	
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public void run() {
		
		final Object bean = this.bean;
		if (bean == null) {
			throw new NullPointerException("No bean.");
		}

		DesignInstance design = null;
		
		PropertyAccessor accessor = session.getTools(
		).getPropertyAccessor();

		ArooaContext parentContext = new DesignSeedContext(
		ArooaType.VALUE, session);

		GenericDesignFactory designFactory =
			new GenericDesignFactory(
					accessor.getClassName(bean));
		
		if (beanConfig == null) {
					
			ArooaElement element = new ArooaElement("bean");
			
			design = designFactory.createDesign(
					element, parentContext);
		}
		else {
			DesignParser parser = new DesignParser(
					designFactory);
			
			try {
				parser.parse(new XMLConfiguration("BEAN-CONFIG", beanConfig));
			} catch (ArooaParseException e) {
				throw new RuntimeException(e);
			}
			
			design = parser.getDesign();
		}
		
		Form form = design.detail();
	
		Component view = SwingFormFactory.create(form).dialog();
		
		final DesignInstance finalDesign = design;
		ValueDialog dialog = new ValueDialog(view, new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				StandardArooaParser parser = new StandardArooaParser(
						bean, session);
				
				try {
					parser.parse(finalDesign.getArooaContext().getConfigurationNode());
				} catch (ArooaParseException e) {
					throw new RuntimeException(e);
				}
				
				XMLArooaParser xmlParser = new XMLArooaParser(session.getArooaDescriptor());
				
				try {
					xmlParser.parse(finalDesign.getArooaContext().getConfigurationNode());
				} catch (ArooaParseException e) {
					throw new RuntimeException(e);
				}
				
				beanConfig = xmlParser.getXml();
				return null;
			}
		});
		dialog.showDialog(null);
	}

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		if (this.bean == null) {
			this.bean = bean;
		}
	}
}
