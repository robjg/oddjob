package org.oddjob.monitor.action;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.FormItem;
import org.oddjob.arooa.design.screem.LabelledComboBox;
import org.oddjob.arooa.design.view.Looks;
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.design.view.SwingFormView;
import org.oddjob.arooa.design.view.SwingItemFactory;
import org.oddjob.arooa.design.view.SwingItemView;
import org.oddjob.arooa.design.view.ViewHelper;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.parsing.QTag;
import org.oddjob.arooa.parsing.QTagConfiguration;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.actions.FormAction;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextSearch;
import org.oddjob.monitor.model.JobFormAction;

/**
 * An action that adds a child job to a job.
 * 
 * @author Rob Gordon 
 */

public class AddJobAction extends JobFormAction implements FormAction {
	
	static {
		
		// Register or form view.
		SwingFormFactory.register(AddJobForm.class, 
				new SwingFormFactory<AddJobForm>() {
			public SwingFormView onCreate(AddJobForm form) {
				return new AddJobFormView(form);
			}	
		});
	}
	
	private LabelledComboBox<QTag> comboBox;
			
	private Form form; 
	
	private ConfigurationSession configurationSession;
	
	private DragPoint dragPoint;
	
	private Object component;
	
	public String getName() {
		return "Add Job";
	}

	public String getGroup() {
		return DESIGN_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.ADD_JOB_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.ADD_JOB_ACCELERATOR_KEY;
	}
	
	@Override
	protected void doPrepare(ExplorerContext explorerContext) {
		
		component = explorerContext.getThisComponent();
		
		ConfigContextSearch search = new ConfigContextSearch();
		configurationSession = search.sessionForAddJob(explorerContext);

		dragPoint = null;
		
		if (configurationSession != null ) {
			dragPoint = configurationSession.dragPointFor(component);
		}

		if (dragPoint == null || !dragPoint.supportsPaste()) {
			setEnabled(false);
			setVisible(false);			
			
			return;
		}

		ArooaDescriptor descriptor = configurationSession.getArooaDescriptor();
		
		InstantiationContext context = new InstantiationContext(
				ArooaType.COMPONENT, new SimpleArooaClass(Object.class));
		
		ArooaElement[] elements = descriptor.getElementMappings().elementsFor(
				context);
		
		SortedSet<QTag> sortedTags = new TreeSet<QTag>();
		
		for (ArooaElement element : elements) {
			
			String prefix = descriptor.getPrefixFor(element.getUri());		
			sortedTags.add(new QTag(prefix, element));
		}
		
		QTag[] allOptions = new QTag[elements.length + 1];		
		allOptions[0] = QTag.NULL_TAG;
		System.arraycopy(sortedTags.toArray(), 0, 
				allOptions, 1, sortedTags.size());
				
		comboBox = new LabelledComboBox<QTag>(allOptions, "New Job");
		
		form = new AddJobForm(comboBox);

		setVisible(true);
		setEnabled(true);
	}
	
	@Override
	protected void doFree(ExplorerContext explorerContext) {
	}
	
	@Override
	protected Form doForm() {
		return form; 
	}

	@Override
	protected void doAction() throws Exception {
	
		QTag selected = comboBox.getSelected();
		
		if (selected == null || selected == QTag.NULL_TAG) {
			return;
		}
		
		QTagConfiguration config = new QTagConfiguration(selected);
		
		XMLArooaParser parser = new XMLArooaParser();
		
		parser.parse(config);
				
		DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
		try {
			dragPoint.paste(-1, parser.getXml());
			trn.commit();
		} catch (Exception e) {
			trn.rollback();
			throw e;
		}
	}
	
	class AddJobForm implements Form {
	
		FormItem formItem;
		
		public AddJobForm(FormItem item) {
			this.formItem = item;
		}
		
		@Override
		public String getTitle() {
			return "Add Job";
		}
		
		public FormItem getFormItem() {
			return formItem;
		}
	}
	
	static class AddJobFormView implements SwingFormView {

		private final AddJobForm standardForm;
		
		public AddJobFormView(AddJobForm form) {
			this.standardForm = form;
		}
		
			
		public Component cell() {
			return ViewHelper.createDetailButton(standardForm);		
		}
		
		public Component dialog() {
			JPanel form = new JPanel();
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

			int items = 1;
			for (int i = 0; i < items ; ++i) {
				c.gridx = 0;
				c.gridy = i + 1;

				JPanel panel = new JPanel();
				panel.setLayout(new GridBagLayout());
				
				SwingItemView itemView = SwingItemFactory.create(
						standardForm.getFormItem());
				
				itemView.inline(panel, 0, 0, false);

				form.add(panel, c);
			}

			// pad the bottom.
			c.weighty = 1.0;
			form.add(new JPanel(), c);
			
			return form;
		}	
	}	
}
