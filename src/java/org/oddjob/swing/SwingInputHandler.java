package org.oddjob.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.oddjob.arooa.design.screem.FileSelectionOptions;
import org.oddjob.arooa.design.view.DialogueHelper;
import org.oddjob.arooa.design.view.FileSelectionWidget;
import org.oddjob.arooa.design.view.Looks;
import org.oddjob.input.InputHandler;
import org.oddjob.input.InputMedium;
import org.oddjob.input.InputRequest;

/**
 * Implementation of an {@link InputHandler} in Swing.
 * 
 * @author rob
 *
 */
public class SwingInputHandler implements InputHandler {

	/** The parent component. Probably the OddjobExplorer window. */
	private Component parent;
	
	/**
	 * Constructor.
	 * 
	 * @param owner The parent component.
	 */
	public SwingInputHandler(Component owner) {
		this.parent = owner;
	}
	
	@Override
	public Properties handleInput(InputRequest[] requests) {
		
		InputDialogue form = new InputDialogue();
		
		List<AtomicReference<String>> refs = 
			new ArrayList<AtomicReference<String>>();
		
		final List<Callable<Boolean>> validations =
				new ArrayList<Callable<Boolean>>();
		
		Properties properties = new Properties();
		
		for (InputRequest request : requests) {

			AtomicReference<String> ref = 
				new AtomicReference<String>();
			refs.add(ref);
			
			FieldBuilder medium = new FieldBuilder(ref);
			request.render(medium);
	
			FormWriter formWriter = medium.getFormWriter();
			form.accept(formWriter);
			
			validations.add(medium.getValidator());
		}
		
		DialogManager dialogManager = new DialogManager();
		dialogManager.showDialog(form.getForm(), new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				for (Callable<Boolean> validator : validations) {
					if (validator == null) {
						continue;
					}
					if (!validator.call()) {
						return Boolean.FALSE;
					}
				}
				return Boolean.TRUE;
			}
		});

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
	
	/**
	 * Builds the input form.
	 */
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
	
	/**
	 * Something that can add a line to the {@link InputDialogue}. 
	 *
	 */
	interface FormWriter {
		
		public int writeTo(Container container, int row);
	}
	
	
	/**
	 * Create a {@link FormWriter} for the different {@link InputMedium}
	 * input types.
	 */
	class FieldBuilder implements InputMedium {
		
		/** Holds the value of the input that will be set on pressing OK. */
		private final AtomicReference<String> reference;
		
		/** Will be created for each input. */
		private FormWriter formWriter;

		/** Validates each input. */
		private Callable<Boolean> validator;
		
		/** Constructor. 
		 *  
		 * @param reference To set the input value with.
		 */
		public FieldBuilder(AtomicReference<String> reference) {
			this.reference = reference;
		}
		
		@Override
		public void confirm(String message, Boolean defaultValue) {
			
			final JLabel label = new JLabel(formatLabelText(message));
			
			final JCheckBox toggle = new JCheckBox();
			
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
					c.anchor = GridBagConstraints.WEST;
					c.gridx = 1;
					c.gridwidth = GridBagConstraints.REMAINDER;
					c.insets = new Insets(3, 3, 3, 3);
					
					container.add(toggle, c);
							
					return row + 1;
				}
			};
			
			validator = new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					reference.set(new Boolean(
							toggle.isSelected()).toString());
					return true;
				}
			};
		}
		
		@Override
		public void password(String prompt) {
			final JLabel label = new JLabel(formatLabelText(prompt));
			
			final JPasswordField text = new JPasswordField(Looks.TEXT_FIELD_SIZE);
						
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
					c.anchor = GridBagConstraints.WEST;
					c.gridx = 1;
					c.gridwidth = GridBagConstraints.REMAINDER;
					c.insets = new Insets(3, 3, 3, 3);
					
					container.add(text, c);
							
					return row + 1;
				}
			};
			
			validator = new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					reference.set(new String(text.getPassword()));
					return true;
				}
			};			
		}
		
		@Override
		public void prompt(String prompt, String defaultValue) {
			
			final JLabel label = new JLabel(formatLabelText(prompt));
			
			final JTextField text = new JTextField(Looks.TEXT_FIELD_SIZE);
			text.setText(defaultValue);
			reference.set(defaultValue);
						
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
					c.anchor = GridBagConstraints.WEST;
					c.gridx = 1;
					c.gridwidth = GridBagConstraints.REMAINDER;
					c.insets = new Insets(3, 3, 3, 3);
					
					container.add(text, c);
							
					return row + 1;
				}
			};
			
			validator = new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					reference.set(new String(text.getText()));
					return true;
				}
			};			

		}
		
		public void message(String message) {
			
			final JLabel label = new JLabel(formatLabelText(message));
			label.setAlignmentY(0.5f);
			
			formWriter = new FormWriter() {
				
				@Override
				public int writeTo(Container container, int row) {
					
					GridBagConstraints c = new GridBagConstraints();

					c.weightx = 0.0;
					c.weighty = 0.0;
					
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.NORTH;
					c.gridx = 0;
					c.gridy = row;
					c.gridwidth = GridBagConstraints.REMAINDER;
					
					c.insets = new Insets(3, 3, 3, 3);		 

					container.add(label, c);
												
					return row + 1;
				}
			};
		}
		
		@Override
		public void file(final String prompt, String defaultValue,
					final FileSelectionOptions options) {

			final JLabel label = new JLabel(formatLabelText(prompt));
			
			final FileSelectionWidget chooser = new FileSelectionWidget();
									
			if (defaultValue != null) {
				chooser.setSelectedFile(defaultValue);
			}
			chooser.setOptions(options);
			
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
					c.anchor = GridBagConstraints.WEST;
					c.gridx = 1;
					c.gridwidth = GridBagConstraints.REMAINDER;
					c.insets = new Insets(3, 3, 3, 3);
					
					container.add(chooser, c);
							
					return row + 1;
				}
			};
			
			
			validator = new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					String chosen = chooser.getSelectedFile();
					if (chosen == null) {
						reference.set(null);
					}
					else {
						reference.set(new File(chosen).getCanonicalPath());
					}
					return Boolean.TRUE;
				}
			};
		}

		public FormWriter getFormWriter() {
			return formWriter;
		}
		
		public Callable<Boolean> getValidator() {
			return validator;
		}
	}
	
	/**
	 * Responsible for displaying the dialogue.
	 */
	class DialogManager {
		
		/** True when OK pressed. */
		private boolean chosen;
		
		
		public boolean isChosen() {
			return chosen;
		}
		
		/**
		 * Show the form on an OK/CANCEL dialogue.
		 * 
		 * @param form The form component.
		 */
		public void showDialog(Component form, Callable<Boolean> okAction) {

			chosen = DialogueHelper.showOKCancelDialogue(
					parent, form, okAction);
		}

	}	
	
	static String formatLabelText(String labelText) {
		
		if (labelText.contains("\n")) {
			return "<html>" + labelText.replaceAll("\\n", "<br/>") + 
					"</html>";
		}
		else {
			return labelText;
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
