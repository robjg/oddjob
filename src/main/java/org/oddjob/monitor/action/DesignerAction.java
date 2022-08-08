/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.action;

import org.oddjob.arooa.*;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.designer.ArooaDesignerForm;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.actions.FormAction;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextSearch;
import org.oddjob.monitor.model.JobFormAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * The action that corresponds to the Oddjob Designer action.
 *
 */
public class DesignerAction extends JobFormAction implements FormAction {

	private static final Logger logger = LoggerFactory.getLogger(DesignerAction.class);

	private ArooaConfiguration config;

	private ArooaDescriptor descriptor;
	
	private ConfigurationHandle<ArooaContext> configHandle;
	
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
			logger.trace("No Parent for {}, no design menu will be visible.",
					explorerContext.getThisComponent());

			setVisible(false);
			setEnabled(false);
		}
		else {
			setVisible(true);
			
			ConfigContextSearch search = new ConfigContextSearch();
			
			ConfigurationSession configSession = search.sessionFor(explorerContext);
			if (configSession == null) {
				logger.trace("No Configuration Session for {}, design menu disabled.",
						explorerContext.getThisComponent());

				setEnabled(false);
			}
			else {
				
				config = configSession.dragPointFor(
						explorerContext.getThisComponent());
				
				if (config == null) {
					logger.trace("No Drag Point for {}, design menu disabled.",
							explorerContext.getThisComponent());

					setEnabled(false);
				}
				else {
					logger.trace("Drag Point found for {}, design menu enabled.",
							explorerContext.getThisComponent());

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