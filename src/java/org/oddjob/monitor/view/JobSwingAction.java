/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.oddjob.arooa.design.actions.AbstractArooaAction;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.design.view.ValueDialog;
import org.oddjob.monitor.actions.ExplorerAction;
import org.oddjob.monitor.actions.FormAction;


public class JobSwingAction extends AbstractArooaAction {
	private static final long serialVersionUID = 20051116;
	private static final Logger logger = Logger.getLogger(JobSwingAction.class);
	private final ExplorerAction jobAction;

	public JobSwingAction(ExplorerAction jobAction) {
		super(jobAction.getName());
		
		putValue(Action.MNEMONIC_KEY, jobAction.getMnemonicKey());
		putValue(Action.ACCELERATOR_KEY, jobAction.getAcceleratorKey());
		
		setEnabled(jobAction.isEnabled());
		setVisible(jobAction.isVisible());
		
		jobAction.addPropertyChangeListener( 
				new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				String propertyName = evt.getPropertyName();
				if (ExplorerAction.ENABLED_PROPERTY.equals(propertyName)) {
					JobSwingAction.this.setEnabled((Boolean) evt.getNewValue());
				}
				else if (ExplorerAction.VISIBLE_PROPERTY.equals(propertyName)){
					JobSwingAction.this.setVisible((Boolean) evt.getNewValue());
				}
			}
		});
		this.jobAction = jobAction;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Component parent = (Component) e.getSource();
		
		try {
			if (jobAction instanceof FormAction) {
				
				Form designDefinition = ((FormAction) jobAction).form();
				
				if (designDefinition != null) {		
					Component form = SwingFormFactory.create(designDefinition).dialog();
					ValueDialog dialog = new ValueDialog(form);
					dialog.showDialog(parent);
					if (!dialog.isChosen()) {
						return;
					}
				}
			}

			jobAction.action();
		}
		catch (Exception ex) {
			logger.warn("Exception performing action.", ex);
			JOptionPane.showMessageDialog(parent, ex, 
					"Exception!", JOptionPane.ERROR_MESSAGE);
		}
	}
}
