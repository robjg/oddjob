/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.oddjob.monitor.model.StateModel;

/**
 *  
 */
public class StatePanel extends JPanel implements Observer {
	private static final long serialVersionUID = 2005010100L;

	private final JTextField stateField = new JTextField(20);

	private final JTextField timeField = new JTextField(20);

	private final JTextArea exceptionField = new JTextArea();

	public StatePanel() {

		stateField.setEditable(false);
		timeField.setEditable(false);
		exceptionField.setEditable(false);
		exceptionField.setLineWrap(false);
		JPanel main = new JPanel();
		
		JLabel l1 = new JLabel("State", JLabel.TRAILING);
		JLabel l2 = new JLabel("Time", JLabel.TRAILING);
		JLabel l3 = new JLabel("Exception", JLabel.TRAILING);
		
		JScrollPane scl = new JScrollPane();
		scl.setViewportView(exceptionField);
		
		main.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(3, 15, 3, 5);
		
		c.gridx = 0;
		c.gridy = 0;
		main.add(l1, c);
		
		c.gridx = 0;
		c.gridy = 1;
		main.add(l2, c);
		
		c.gridx = 0;
		c.gridy = 2;
		main.add(l3, c);
				
		c.insets = new Insets(3, 5, 3, 5);
		c.weightx = 1.0;
		
		c.gridx = 1;
		c.gridy = 0;
		main.add(stateField, c);

		c.gridx = 1;
		c.gridy = 1;
		main.add(timeField, c);

		c.fill = GridBagConstraints.BOTH;	
		c.weighty = 1.0;
		c.gridx = 1;
		c.gridy = 2;
		main.add(scl, c);

		JScrollPane formScroll = new JScrollPane();
		formScroll.setViewportView(main);
		
		setLayout(new BorderLayout());
		add(formScroll, BorderLayout.CENTER);
		
//		c.insets = new Insets(0, 0, 0, 0);
//		c.gridx = 2;
//		c.gridy = 0;
//		c.gridheight = GridBagConstraints.REMAINDER;
//		c.weightx = 1.0;
//		c.weighty = 0.0;
//		JComponent padding1 = new JPanel();
//		add(padding1, c);
		
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		final StateModel model = (StateModel) o;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {		
				stateField.setText(model.getState());
				timeField.setText(model.getTime());
				exceptionField.setText(model.getException());
			}
		});	
	}
	
}
