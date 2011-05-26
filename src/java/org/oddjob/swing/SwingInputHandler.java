package org.oddjob.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.oddjob.arooa.design.view.Looks;
import org.oddjob.arooa.design.view.ViewHelper;
import org.oddjob.input.InputHandler;
import org.oddjob.input.InputMedium;
import org.oddjob.input.InputRequest;

public class SwingInputHandler implements InputHandler {

	private Component parent;
	
	public SwingInputHandler(Component owner) {
		this.parent = owner;
	}
	
	@Override
	public Properties handleInput(InputRequest[] requests) {
		
		InputDialogue form = new InputDialogue();
		
		List<AtomicReference<String>> refs = 
			new ArrayList<AtomicReference<String>>();
		
		Properties properties = new Properties();
		
		for (InputRequest request : requests) {

			AtomicReference<String> ref = 
				new AtomicReference<String>();
			refs.add(ref);
			
			FieldBuilder medium = new FieldBuilder(ref);
			request.render(medium);
	
			FormWriter formWriter = medium.getFormWriter();
			form.accept(formWriter);
		}
		
		DialogManager dialogManager = new DialogManager();
		dialogManager.showDialog(form.getForm());

		if (dialogManager.isChosen()) {
			int i = 0;
			for (AtomicReference<String> ref : refs) {
				String property = requests[i++].getProperty();
				if (property == null) {
					continue;
				}
				if (ref.get() == null) {
					continue;
				}
				properties.setProperty(property, ref.get());
			}
			return properties;
		}
		else {
			return null;
		}
	}
	
	class InputDialogue {
		
		private final JPanel form = new JPanel();
		
		private int row;
		
		public InputDialogue() {
			
			form.setLayout(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTHWEST;

			c.insets = new Insets(Looks.DETAIL_FORM_BORDER, 
					Looks.DETAIL_FORM_BORDER, 
					Looks.DETAIL_FORM_BORDER, 
					Looks.DETAIL_FORM_BORDER);

			c.gridx = 0;
			c.gridy = 0;
		}
		
		public void accept(FormWriter formWriter) {
		
			row = formWriter.writeTo(form, row);
		}
		
		public JPanel getForm() {
			return form;
		}
	}
	
	interface FormWriter {
		
		public int writeTo(Container container, int row);
	}
	
	
	class FieldBuilder implements InputMedium {
		
		private final AtomicReference<String> reference;
		
		private FormWriter formWriter;

		
		public FieldBuilder(AtomicReference<String> reference) {
			this.reference = reference;
		}
		
		@Override
		public void confirm(String message, Boolean defaultValue) {
			
			final JLabel label = new JLabel(message);
			
			final JToggleButton toggle = new JToggleButton();
			toggle.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					reference.set(new Boolean(
							toggle.isSelected()).toString());
				}
			});
			if (defaultValue != null) {
				toggle.setSelected(defaultValue.booleanValue());
			}
						
			formWriter = new FormWriter() {
				
				@Override
				public int writeTo(Container container, int row) {
					
					GridBagConstraints c = new GridBagConstraints();

					c.weightx = 0.3;
					c.weighty = 0.0;
					
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.NORTHWEST;
					c.gridx = 0;
					c.gridy = row;
					
					c.insets = new Insets(3, 3, 3, 20);		 

					container.add(label, c);
					
					c.weightx = 1.0;
					c.fill = GridBagConstraints.NONE;
					c.anchor = GridBagConstraints.NORTHWEST;
					c.gridx = 1;
					c.gridwidth = GridBagConstraints.REMAINDER;
					c.insets = new Insets(3, 0, 3, 0);
					
					container.add(toggle, c);
							
					return row + 1;
				}
			};
		}
		
		@Override
		public void password(String prompt) {
			final JLabel label = new JLabel(ViewHelper.padLabel(prompt), 
					SwingConstants.LEADING);
			
			final JPasswordField text = new JPasswordField(Looks.TEXT_FIELD_SIZE);
						
			text.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					reference.set(new String(text.getPassword()));
				}
				public void removeUpdate(DocumentEvent e) {
					reference.set(new String(text.getPassword()));
				}
				public void insertUpdate(DocumentEvent e) {
					reference.set(new String(text.getPassword()));
				}
			});		
			
			formWriter = new FormWriter() {
				
				@Override
				public int writeTo(Container container, int row) {
					
					GridBagConstraints c = new GridBagConstraints();

					c.weightx = 0.3;
					c.weighty = 0.0;
					
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.NORTHWEST;
					c.gridx = 0;
					c.gridy = row;
					
					c.insets = new Insets(3, 3, 3, 20);		 

					container.add(label, c);
					
					c.weightx = 1.0;
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.NORTHWEST;
					c.gridx = 1;
					c.gridwidth = GridBagConstraints.REMAINDER;
					c.insets = new Insets(3, 0, 3, 0);
					
					container.add(text, c);
							
					return row + 1;
				}
			};
			
		}
		
		@Override
		public void prompt(String prompt, String defaultValue) {
			
			final JLabel label = new JLabel(ViewHelper.padLabel(prompt), 
					SwingConstants.LEADING);
			
			final JTextField text = new JTextField(Looks.TEXT_FIELD_SIZE);
			text.setText(defaultValue);
			reference.set(defaultValue);
			
			text.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					reference.set(text.getText());
				}
				public void removeUpdate(DocumentEvent e) {
					reference.set(text.getText());
				}
				public void insertUpdate(DocumentEvent e) {
					reference.set(text.getText());
				}
			});		
			
			formWriter = new FormWriter() {
				
				@Override
				public int writeTo(Container container, int row) {
					
					GridBagConstraints c = new GridBagConstraints();

					c.weightx = 0.3;
					c.weighty = 0.0;
					
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.NORTHWEST;
					c.gridx = 0;
					c.gridy = row;
					
					c.insets = new Insets(3, 3, 3, 20);		 

					container.add(label, c);
					
					c.weightx = 1.0;
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.NORTHWEST;
					c.gridx = 1;
					c.gridwidth = GridBagConstraints.REMAINDER;
					c.insets = new Insets(3, 0, 3, 0);
					
					container.add(text, c);
							
					return row + 1;
				}
			};
		}
		
		public void message(String message) {
			
			final JLabel label = new JLabel(message);
			label.setAlignmentY(0.5f);
			
			formWriter = new FormWriter() {
				
				@Override
				public int writeTo(Container container, int row) {
					
					GridBagConstraints c = new GridBagConstraints();

					c.weightx = 0.3;
					c.weighty = 0.0;
					
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.NORTHWEST;
					c.gridx = 0;
					c.gridy = row;
					c.gridwidth = 2;
					
					c.insets = new Insets(3, 3, 3, 20);		 

					container.add(label, c);
												
					return row + 1;
				}
			};
		}

		public FormWriter getFormWriter() {
			return formWriter;
		}
	}
	
	class DialogManager {
		
		private boolean chosen;
		
		public boolean isChosen() {
			return chosen;
		}
		
		public void showDialog(Component form) {

			final JDialog dialog;  
			
			if (parent != null) {
				Window w = ViewHelper.getWindowForComponent(parent);
				
				if (w instanceof Frame) {
					dialog = new JDialog((Frame) w);
				} else {
					dialog = new JDialog((Dialog) w);
				}
				dialog.setLocationRelativeTo(w);
			}
			else {
				dialog = new JDialog();
			}
			
			dialog.getContentPane().setLayout(new BorderLayout());
			
			dialog.getContentPane().add(form, BorderLayout.CENTER);	

			JPanel selection = new JPanel();
			
			JButton ok = new JButton("OK");
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
					chosen = true;
				}
			});
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
					chosen = false;
				}
			});
			
			selection.add(ok);
			selection.add(cancel);

			dialog.getContentPane().add(selection, BorderLayout.PAGE_END);
			
			dialog.setDefaultCloseOperation(
					WindowConstants.DISPOSE_ON_CLOSE);
			
			dialog.setModal(true);
			
			dialog.pack();
			dialog.setVisible(true);
		}

	}	
}
