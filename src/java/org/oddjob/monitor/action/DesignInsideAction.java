/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.oddjob.Oddjob;
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
import org.oddjob.designer.components.RootDC;
import org.oddjob.monitor.actions.FormAction;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;

/**
 * The action that Designs the inside of a 
 * {@link ConfigurationOwner}.
 *
 * @author rob
 */
public class DesignInsideAction extends JobAction implements FormAction {
	private static final long serialVersionUID = 2008120400;
	
	private ArooaConfiguration config;

	private ArooaDescriptor descriptor;
	
	private ConfigurationHandle configHandle;
	
	public String getName() {
		return "Design Inside";
	}
	
	public String getGroup() {
		return DESIGN_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return null;
	}
	
	public KeyStroke getAcceleratorKey() {
		return null;
	}

	@Override
	protected void doPrepare(ExplorerContext context) {
					
			ConfigurationOwner configOwner = null;
			if (context.getThisComponent() instanceof ConfigurationOwner) {
				configOwner = 
					(ConfigurationOwner) context.getThisComponent();
			}
			
			if (configOwner == null) {
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
						descriptor = configOwner.provideConfigurationSession(
								).getArooaDescriptor();
						
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
	
	public Form form() {
		
		ArooaSession session = new StandardArooaSession(descriptor);
		
		//TODO: Need to re-think this when foreach becomes a ConfigurationOwner!!!
		DesignParser parser = new DesignParser(session, new RootDC());
		parser.setExpectedDoucmentElement(Oddjob.ODDJOB_ELEMENT);
		parser.setArooaType(ArooaType.COMPONENT);
				
		try {
			configHandle = parser.parse(config);			
		} catch (ArooaParseException e) {
			throw new RuntimeException(e);
		}
		
		return new ArooaDesignerForm(parser);
	}
}