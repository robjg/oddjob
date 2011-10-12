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
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.actions.FormAction;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextSearch;
import org.oddjob.monitor.model.JobFormAction;

/**
 * The action that corresponds to the Oddjob Designer action.
 *
 */
public class DesignerAction extends JobFormAction implements FormAction {
	private static final long serialVersionUID = 2008120400;
	
	private ArooaConfiguration config;

	private ArooaDescriptor descriptor;
	
	private ConfigurationHandle configHandle;
	
	public String getName() {
		return "Designer";
	}
	
	public String getGroup() {
		return DESIGN_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.DESIGNER_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.DESIGNER_ACCELERATOR_KEY;
	}
	
	@Override
	protected void doPrepare(ExplorerContext explorerContext) {
		
		if (explorerContext.getParent() == null) {
			setVisible(false);
			setEnabled(false);
		}
		else {
			setVisible(true);
			
			ConfigContextSearch search = new ConfigContextSearch();
			
			ConfigurationSession configSession = search.sessionFor(explorerContext);
			if (configSession == null) {
				setEnabled(false);
			}
			else {
				
				config = configSession.dragPointFor(
						explorerContext.getThisComponent());
				
				if (config == null) {
					setEnabled(false);
				}
				else {
					descriptor = configSession.getArooaDescriptor();
					
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
		
		ArooaSession session = new StandardArooaSession(descriptor);
		
		DesignParser parser = new DesignParser(session);
		parser.setArooaType(ArooaType.COMPONENT);
				
		try {
			configHandle = parser.parse(config);			
		} catch (ArooaParseException e) {
			throw new RuntimeException(e);
		}
		
		return new ArooaDesignerForm(parser);
	}
}