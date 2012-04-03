/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import javax.swing.Action;

import org.oddjob.arooa.design.actions.AbstractArooaAction;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.view.DialogueHelper;
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.design.view.ValueDialog;
import org.oddjob.monitor.actions.ExplorerAction;
import org.oddjob.monitor.actions.FormAction;


public class JobSwingAction extends AbstractArooaAction {
	private static final long serialVersionUID = 20051116;
	
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
		
		if (jobAction instanceof FormAction) {

			Form designDefinition = ((FormAction) jobAction).form();

			if (designDefinition != null) {		
				Component form = SwingFormFactory.create(designDefinition).dialog();
				ValueDialog dialog = new ValueDialog(form, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						jobAction.action();
						return true;
					}
				});
				dialog.showDialog(parent);
			}
		}
		else {
			try {
				jobAction.action();
			}
			catch (Exception ex) {
				DialogueHelper.showExceptionMessage(parent, ex);
			}
		}
	}
}
