package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignSeedContext;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TextField;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.runtime.ConfigurationNode;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.actions.FormAction;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextSearch;
import org.oddjob.monitor.model.JobFormAction;

/**
 * An action that sets a property on a job.
 * 
 * @author Rob Gordon 
 */

public class SetPropertyAction extends JobFormAction implements FormAction {
	private static final Logger logger = Logger.getLogger(SetPropertyAction.class);
	
	/** The job */
	private Object job = null;
	
	private PropertyForm propertyForm; 
	
	private ConfigurationSession sessionLite;
	
	public String getName() {
		return "Set Property";
	}

	public String getGroup() {
		return PROPERTY_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.PROPERTY_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.PROPERTY_ACCELERATOR_KEY;
	}
	
	@Override
	protected void doPrepare(ExplorerContext explorerContext) {
		
		// No writable properties on Oddjob root.
		if(explorerContext.getParent() != null) {
			
			setVisible(true);
			
			Object component = explorerContext.getThisComponent();
			
			// Remote connection is read only.
			if (component instanceof RemoteOddjobBean && 
					!(component instanceof DynaBean)) {
				setEnabled(false);
			}
			else {
				ConfigContextSearch search = new ConfigContextSearch();
				sessionLite = search.sessionFor(explorerContext);
		
				if (sessionLite == null) {
					setEnabled(false);
				}
				else {
					job = component;
					
					DesignSeedContext context = new DesignSeedContext(
							ArooaType.VALUE,
							new StandardArooaSession(sessionLite.getArooaDescriptor()));
					
					propertyForm = new PropertyForm(new ArooaElement("property"), context);
					
					setEnabled(true);
				}
			}
		}
		else {
			setVisible(false);
			setEnabled(false);			
		}
	}
	
	@Override
	protected void doFree(ExplorerContext explorerContext) {
	}
	
	@Override
	public Form doForm() {
		return propertyForm.detail(); 
	}

	@Override
	protected void doAction() throws Exception {
		
		ConfigurationNode valueConfiguration = propertyForm.getArooaContext().getConfigurationNode();

		if (logger.isDebugEnabled()) {
			XMLArooaParser xml = new XMLArooaParser();
			xml.parse(valueConfiguration);
			logger.debug("PropertyForm XML:\n" + xml.getXml());
		}
		
		PropertyCapture propertyCapture = new PropertyCapture();

		ArooaSession session = new StandardArooaSession(
				sessionLite.getArooaDescriptor());
		
		StandardArooaParser parser = new StandardArooaParser(propertyCapture, 
				session);		
		try {
			parser.parse(valueConfiguration);
		} catch (ArooaParseException ex) {
			throw new RuntimeException(ex);
		}

		session.getComponentPool().configure(propertyCapture);
		
		String name = propertyCapture.getName();
		
		if (name == null || "".equals(name.trim())) {
			logger.debug("No name.");
			return;
		}

		PropertyAccessor accessor = session.getTools().getPropertyAccessor(
				).accessorWithConversions(
						session.getTools().getArooaConverter());
		
		accessor.setSimpleProperty(job, name, propertyCapture.getValue());
		
	}
	
	class PropertyForm extends DesignValueBase {

		SimpleTextAttribute name;
		
		SimpleDesignProperty value; 
		
		public PropertyForm(ArooaElement element, ArooaContext parentContext) {
			super(element, new SimpleArooaClass(PropertyCapture.class), 
					parentContext);
		
			name = new SimpleTextAttribute("name", this);
			
			value = new SimpleDesignProperty(
					"value", Object.class, ArooaType.VALUE, this);		
			
		}
		
		public DesignProperty[] children() {
			return new DesignProperty[] { name, value } ;
		} 
		
		public Form detail() {
			return new StandardForm("Set Property", this)
				.addFormItem(
					new BorderedGroup("Property")
						.add(new TextField("Name", name))
						.add(value.view().setTitle("Value")));
		}

	}
	
	public class PropertyCapture {
		
		private String name;
		
		private Object value;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
		
	}
}
