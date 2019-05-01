/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.designer.ArooaDesignerForm;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.actions.FormAction;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobFormAction;

/**
 * The action that Designs the inside of a 
 * {@link ConfigurationOwner}.
 *
 * @author rob
 */
public class DesignInsideAction extends JobFormAction implements FormAction {
	
	private ArooaConfiguration config;

	private DesignParser parser;
	
	private ConfigurationHandle configHandle;
	
	public String getName() {
		return "Design Inside";
	}
	
	public String getGroup() {
		return DESIGN_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.DESIGN_INSIDE_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.DESIGNER_INSIDE_ACCELERATOR_KEY;
	}

	@Override
	protected void doPrepare(ExplorerContext context) {
					
			ConfigurationOwner configOwner = null;
			if (context.getThisComponent() instanceof ConfigurationOwner) {
				configOwner = 
					(ConfigurationOwner) context.getThisComponent();
			}
			
			if (configOwner == null || 
					configOwner.rootDesignFactory() == null) {
				setEnabled(false);
				setVisible(false);
			}
			else {
				setVisible(true);
				
				ConfigurationSession configSession = configOwner.provideConfigurationSession();
				
				if (configSession == null) {
					setEnabled(false);
				}
				else {
					
					config = configSession.dragPointFor(
							context.getThisComponent());
					
					if (config == null) {
						setEnabled(false);
					}
					else {
						
						ArooaDescriptor descriptor = configOwner.provideConfigurationSession(
								).getArooaDescriptor();
						
						ArooaSession session = new StandardArooaSession(descriptor);
						
						parser = new DesignParser(session, configOwner.rootDesignFactory());
						
						parser.setExpectedDocumentElement(configOwner.rootElement());
						parser.setArooaType(ArooaType.COMPONENT);
								
						
						setEnabled(true);
					}
				}
			}
	}
	
	@Override
	protected void doFree(ExplorerContext explorerContext) {
	}
	
	@Override
	protected void doAction() throws Exception {
		configHandle.save();
	}
	
	@Override
	protected Form doForm() {
		
		try {
			configHandle = parser.parse(config);			
		} catch (ArooaParseException e) {
			throw new RuntimeException(e);
		}
		
		return new ArooaDesignerForm(parser);
	}
}