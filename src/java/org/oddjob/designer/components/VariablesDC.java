/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.oddjob.arooa.ArooaConstants;
import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.ElementMappings;
import org.oddjob.arooa.design.DesignComponent;
import org.oddjob.arooa.design.DesignElementProperty;
import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignListener;
import org.oddjob.arooa.design.DesignStructureEvent;
import org.oddjob.arooa.design.InstanceSupport;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.FormItem;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.view.DesignViewException;
import org.oddjob.arooa.design.view.Looks;
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.design.view.SwingFormView;
import org.oddjob.arooa.design.view.SwingItemFactory;
import org.oddjob.arooa.design.view.SwingItemView;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.AbstractConfigurationNode;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ArooaHandler;
import org.oddjob.arooa.parsing.PrefixMappings;
import org.oddjob.arooa.parsing.QTag;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.runtime.AbstractRuntimeConfiguration;
import org.oddjob.arooa.runtime.ConfigurationNode;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.values.VariablesJob;

/**
 *
 */
public class VariablesDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new VariablesDesign(element, parentContext);
	}
}

class VariablesDesign implements DesignComponent {

	final List<PropertyValuePair> properties = 
		new ArrayList<PropertyValuePair>();

	private String id;

	private ArooaContext context;
	
	private ArooaElement element;
	
	private List<VariablesListener> listeners =
		new ArrayList<VariablesListener>();
	
	public VariablesDesign(ArooaElement element, ArooaContext parentContext) {
		this.element = element;
		
		this.id = element.getAttributes().get(ArooaConstants.ID_PROPERTY);
		
		this.context = new VariablesDesignContext(this, parentContext);
	}
	
	public ArooaElement element() {
		return element;
	}
	
	
	public Form detail() {
		return new StandardForm(this)
					.addFormItem(new BorderedGroup("Variables")
						.add(new VariablesGrid(this)));
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	public void addStructuralListener(DesignListener listener) {
		// No child components so nothing to do here.
	}
	
	public void removeStructuralListener(DesignListener listener) {
	}
	
	public ArooaContext getArooaContext() {
		return context;
	}
	
	@Override
	public String toString() {
		if (id == null) {
			return "VariablesJob";
		}
		return id;
	}
	
	void addVariablesListener(VariablesListener listener) {
		synchronized (listeners) {
			for (int i = 0; i < properties.size(); ++i) {
				listener.variableAdded(i);
			}
			listeners.add(listener);
		}
	}
	
	void addProperty(int index, DesignElementProperty property) {
		synchronized (listeners) {
			properties.add(index, new PropertyValuePair(property));
			for (VariablesListener listener: listeners) {
				listener.variableAdded(index);
			}
		}
	}
	
	void removeProperty(int index) {
		synchronized (listeners) {
			properties.remove(index);
			for (VariablesListener listener: listeners) {
				listener.variableRemoved(index);
			}
		}
	}
	
	DesignInstance instanceAt(int index) {
		return properties.get(index).getValue();
	}
	
	DesignElementProperty propertyAt(int index) {
		return properties.get(index).getProperty();
	}
	
	int propertyCount() {
		return properties.size();
	}
}

class PropertyValuePair { 

	private final DesignElementProperty property;
	
	private DesignInstance value;
	
	PropertyValuePair(DesignElementProperty property) {
		this.property = property;
		property.addDesignListener(new DesignListener() {
			public void childAdded(DesignStructureEvent event) {
				value = event.getChild();
			}
			public void childRemoved(DesignStructureEvent event) {
				value = null;
			}
		});
	}

	DesignElementProperty getProperty() {
		return property;
	}
	
	DesignInstance getValue() {
		return value;
	}
	
}
	
interface VariablesListener {
	
	void variableAdded(int index);
	
	void variableRemoved(int index);
}

class VariablesDesignContext implements ArooaContext {
	
	private final ArooaContext parent;
	
	private final VariablesDesign variables;
	
	private final ConfigurationNode configurationNode = new AbstractConfigurationNode() {

		public ArooaContext getContext() {
			return VariablesDesignContext.this;
		}
		
		public void addText(String text) {
			String trimmedText = text.trim(); 
			if (trimmedText.length() > 0) {
				throw new ArooaException("No text expected: " + trimmedText);				
			}
		}
		
		public ConfigurationHandle parse(ArooaContext parentContext)
				throws ArooaParseException {
			
			ArooaElement element = new ArooaElement(
					variables.element().getUri(),
					variables.element().getTag());
			
			String id = variables.getId();
    		if (id != null && id.length() > 0) {
    			element = element.addAttribute(
    					ArooaConstants.ID_PROPERTY, id);
    		}

    		ArooaContext nextContext = parentContext.getArooaHandler().onStartElement(
    				element, parentContext);

    		for (int i = 0; i < variables.propertyCount(); ++i) {
    			DesignElementProperty property = variables.propertyAt(i); 
    			property.getArooaContext().getConfigurationNode().parse(nextContext);
    		}

    		int index = parentContext.getConfigurationNode().insertChild(
    				nextContext.getConfigurationNode());
    		
    		try {
    			nextContext.getRuntime().init();
    		}
    		catch (RuntimeException e) {
    			parentContext.getConfigurationNode().removeChild(index);
    			throw e;
    		}
    		
    		return new ChainingConfigurationHandle(
    				getContext(), parentContext, index);
		}
		
	};

	private final RuntimeConfiguration runtime = new AbstractRuntimeConfiguration() {

		public void init() {
			fireBeforeInit();
			
			RuntimeConfiguration parentRuntime = parent.getRuntime();
			
			// check it's not the root
			if (parentRuntime != null) {
				
				int index = parent.getConfigurationNode().indexOf(
						configurationNode);
			
				if (index < 0) {
					throw new IllegalStateException(
							"Configuration node not added to parent.");
				}
				
				parentRuntime.setIndexedProperty(null, index, variables);
			}
			
			fireAfterInit();
		}
		
		public void configure() {
			fireBeforeConfigure();
			fireAfterConfigure();
		}
		
		public void destroy() {
			fireBeforeDestroy();
			
			RuntimeConfiguration parentRuntime = parent.getRuntime();
			
			// check it's not the root
			if (parentRuntime != null) {
				
				int index = parent.getConfigurationNode().indexOf(
						configurationNode);

				if (index < 0) {
					throw new IllegalStateException(
							"Configuration node not added to parent.");
				}

				parentRuntime.setIndexedProperty(null, index, null);
			}

			fireAfterDestroy();
		}
		
		public void setProperty(String name, Object value) throws ArooaException {
			throw new UnsupportedOperationException("Properties are fixed in Design Mode.");
		}
		
		public void setIndexedProperty(String name, int index, Object value)
				throws ArooaException {
			if (value == null) {
				variables.removeProperty(index);
			}
			else {
				variables.addProperty(index, (DesignElementProperty) value);
			}
		}
		
		public void setMappedProperty(String name, String key, Object value)
				throws ArooaException {
			throw new UnsupportedOperationException("Properties are fixed in Design Mode.");
		}
		
		public ArooaClass getClassIdentifier() {
			return new SimpleArooaClass(VariablesJob.class);
		}
	};
	
	VariablesDesignContext(
			VariablesDesign variables, 
			ArooaContext parent) {
		this.parent = parent;
		this.variables = variables;

	}

	public ArooaType getArooaType() {
		return ArooaType.VALUE;
	}
	
	public ArooaContext getParent() {
		return parent;
	}
	
	public RuntimeConfiguration getRuntime() {
		return runtime;
	}
	
	public PrefixMappings getPrefixMappings() {
		return parent.getPrefixMappings();
	}
	
	public ArooaSession getSession() {
		return parent.getSession();
	}
	
	public ConfigurationNode getConfigurationNode() {
		return configurationNode;
	}
	
	public ArooaHandler getArooaHandler() {
		return new ArooaHandler() {
			
			public ArooaContext onStartElement(ArooaElement element,
					ArooaContext parentContext) throws ArooaException {

				if (variables.properties.contains(element.getTag())) {
					throw new ArooaException("Duplicate property " + element);
				}
				
				if (element.getAttributes().getAttributNames().length > 0) {
					throw new ArooaException("Property can't contain attributes: " + 
							element.getAttributes().getAttributNames()[0]);
				}
				
				SimpleDesignProperty property = new SimpleDesignProperty(
						element.getTag(), Object.class, 
						ArooaType.VALUE, variables);
				
				return property.getArooaContext();
			}
		};
	}
		
}

class VariablesGrid implements FormItem {

	static {
		SwingItemFactory.register(VariablesGrid.class, 
				new SwingItemFactory<VariablesGrid>() {
			public SwingItemView onCreate(VariablesGrid viewModel) {
				return new VariablesTableView(viewModel);
			}	
		});
	}
	
	private String title;
	
	private final VariablesDesign variables;
	
	public VariablesGrid(VariablesDesign variables) {
		this.variables = variables;
	}
	
	public VariablesDesign getVariables() {
		return variables;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isPopulated() {
		return true;
	}
	
	public FormItem setTitle(String title) {
		this.title = title;
		return this;
	}
}


class VariablesTableView implements SwingItemView {

	private static final int DEFAULT_TABLE_ROWS = 12;
	
	public static final QTag NULL_TAG = new QTag("");
	
	private final VariablesTableModel tableModel = new VariablesTableModel();
	
	private final VariablesDesign variables;

	private JLabel label;
	
	private Component component;
		
	public VariablesTableView(VariablesGrid variablesGrid) {
		variables = variablesGrid.getVariables();
		
		this.component = component();
		label = new JLabel(variablesGrid.getTitle());
		
		variables.addVariablesListener(new VariablesListener() {
			public void variableAdded(int index) {
				tableModel.fireTableChanged(new TableModelEvent(tableModel));
				variables.propertyAt(index).addDesignListener(new DesignListener() {
					public void childAdded(DesignStructureEvent event) {
						tableModel.fireTableChanged(new TableModelEvent(tableModel));
					}
					public void childRemoved(DesignStructureEvent event) {
						tableModel.fireTableChanged(new TableModelEvent(tableModel));
					}
				});
			}
			public void variableRemoved(int index) {
				tableModel.fireTableChanged(new TableModelEvent(tableModel));
			}			
		});		
	}

	/* (non-Javadoc)
	 * @see org.oddjob.designer.view.ViewProducer#inline(java.awt.Container, int, int, boolean)
	 */
	public int inline(Container container, int row, int column,
			boolean selectionInGroup) {
		
		GridBagConstraints c = new GridBagConstraints();

		if (label != null) {
			c.weightx = 1.0;
			c.weighty = 0.0;
			
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridx = column;
			c.gridy = row;
			if (selectionInGroup) {
				c.gridwidth = 2;
			}
			
			c.insets = new Insets(3, 3, 3, 20);		 
	
			container.add(label, c);
			
			++row;
		}
		
		c.weightx = 1.0;
		c.weighty = 0.0;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		
		c.gridx = column;
		c.gridy = row;
		
		c.gridwidth = GridBagConstraints.REMAINDER;
				
		c.insets = new Insets(3, 3, 3, 3);		 

		container.add(component, c);
		
		return row + 1;
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.designer.view.ViewProducer#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		// Currently not used as the view will not be part of a selection. 
		component.setEnabled(enabled);
	}

	/**
	 * Creates the table component.
	 * 
	 * @return The table.
	 */
	private Component component() {
		JTable table = new JTable(tableModel);
		table.setRowHeight(new JTextField().getPreferredSize().height);
		
		setPreferredTableSize(table);
		
		TableColumn typeCol = table.getColumnModel().getColumn(1);
		
		JComboBox comboBox = new JComboBox();
		QTag[] types = getOptions();
		for (int i = 0; i < types.length; ++i) {
			comboBox.addItem(types[i]);				
		}
		typeCol.setCellEditor(new DefaultCellEditor(comboBox));

		TableColumn valueCol = table.getColumnModel().getColumn(
				tableModel.getColumnCount() - 1);
		valueCol.setCellEditor(new DialogEditor());
		valueCol.setCellRenderer(new DialogRenderer());

		JScrollPane jsp = new JScrollPane(table);
		return jsp;
	}
		
	private void setPreferredTableSize(JTable table){ 
	    int height = 0; 
	    for(int row=0; row < DEFAULT_TABLE_ROWS; row++) 
	        height += table.getRowHeight(row); 
	 
	    table.setPreferredScrollableViewportSize(new Dimension( 
	            Looks.DETAIL_USABLE_WIDTH - 100,
	            height)); 
	}
			
	class VariablesTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 2008100100;

		String[] headers = { "Name", "Type", "Value" };

		public String getColumnName(int c) {
				return headers[c];
		}

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return variables.properties.size() + 1;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return true;
			}
			if (!"".equals(getValueAt(rowIndex, 0))) {
				return true;
			}
			return false;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex == getRowCount() - 1) {
				return "";
			}
			
			if (columnIndex == 0) {
				return variables.propertyAt(rowIndex).property();
			} else if (columnIndex == 1) {
				DesignInstance design = variables.instanceAt(rowIndex);
				if (design != null) {
					return InstanceSupport.tagFor(design);
				}
				else {
					return NULL_TAG;
				}
			} else if (columnIndex == 2) {
				return variables.instanceAt(rowIndex);
			} else {
				throw new RuntimeException("This should be impossible!");
			}
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				String newValue = ((String) value).trim();
				if (rowIndex < getRowCount() - 1) {
					
					String oldProperty = variables.propertyAt(rowIndex).property();
					
					if (newValue.equals(oldProperty)) {
						// if the type hasn't changed then nothing to do.
						return;
					}
					else {
						removeProperty(variables.propertyAt(rowIndex));
					}
				}
				if ("".equals(newValue)) {
					return;
				}
				insertProperty(rowIndex, newValue);
			}
			else if (columnIndex == 1) {
				if (rowIndex == getRowCount() - 1) {
					// no property
					return;
				}

				DesignElementProperty designProperty =
					variables.propertyAt(rowIndex);
				
				InstanceSupport support = new InstanceSupport(
						designProperty);
				
				QTag type = (QTag) value;
					
				DesignInstance oldInstance = variables.instanceAt(rowIndex);
				QTag oldType;
				if (oldInstance == null) {
					oldType = NULL_TAG;
				}
				else {
					oldType = InstanceSupport.tagFor(oldInstance);
				}
					
				if (type.equals(oldType)) {
					// if the type hasn't changed then nothing to do.
					return;
				}
				else if (!NULL_TAG.equals(oldType)){
					support.removeInstance(oldInstance);
				}
					
				if (NULL_TAG.equals(type)) {
					return;
				}
				try {
					support.insertTag(0, type);
				}
				catch (ArooaParseException e) {
					throw new DesignViewException(e);
				}
				
			} else if (columnIndex == 2) {
				// don't do anything - the render takes care of the display.
			} else {
				throw new RuntimeException("This should be impossible!");
			}
		}

	}
	
	/**
	 * The cell editor for the instance.
	 * 
	 */
	public class DialogEditor extends AbstractCellEditor 
	implements TableCellEditor {
		private static final long serialVersionUID = 20081008;
		
		DesignInstance value;

		//Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue() {
			return value;
		}

		//Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			this.value = (DesignInstance) value;
			return generateRenderer((DesignInstance) value);
		}
	}

	/**
	 * The value renderer.
	 *
	 */
	class DialogRenderer implements TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value == null) {
				JLabel label = new JLabel();
				label.setEnabled(false);
				return label;
			}
			else if (value instanceof String) {
				return new JLabel(value.toString());
			} else {
				return generateRenderer((DesignInstance) value);
			}
		}
	}
	
	/**
	 * Generate the table cell component used to edit the design of 
	 * the instance.
	 * 
	 * @param designInstance
	 * @return
	 */
	static Component generateRenderer(DesignInstance designInstance) {
		Form designDefintion = designInstance.detail();
		SwingFormView view = SwingFormFactory.create(designDefintion);
		
		return view.cell();
	}
	
	QTag[] getOptions() {
		
		ArooaContext context = variables.getArooaContext(); 
		
		ElementMappings mappings = context.getSession(
				).getArooaDescriptor().getElementMappings();
		
		InstantiationContext instantiationContext = 
			new InstantiationContext(ArooaType.VALUE, 
					new SimpleArooaClass(ArooaValue.class));
		
		ArooaElement[] supportedElements = 
			mappings.elementsFor(instantiationContext);
				
		QTag[] supportedTypes = new QTag[supportedElements.length + 1]; 
		
		supportedTypes[0] = NULL_TAG;
		
		for (int i = 1; i < supportedTypes.length; ++i) {
			supportedTypes[i] = new QTag(
					supportedElements[i-1], context);
		}
		
		return supportedTypes;
	}
	
	void removeProperty(DesignElementProperty property) {
	
		property.getArooaContext().getRuntime().destroy();
		
		ConfigurationNode configurationNode = variables.getArooaContext().getConfigurationNode();

		int index = configurationNode.indexOf(property.getArooaContext().getConfigurationNode());
		
		if (index < 0) {
			throw new IllegalStateException("Configuration node is not a child of the variables context.");
		}
		
		configurationNode.removeChild(index);
	}
	
	void insertProperty(int index, String property) {
		
		ArooaContext nextContext = variables.getArooaContext().getArooaHandler().onStartElement(
				new ArooaElement(property), variables.getArooaContext());
		
		variables.getArooaContext().getConfigurationNode().setInsertPosition(index);
		
		int i = variables.getArooaContext().getConfigurationNode().insertChild(
				nextContext.getConfigurationNode());
		
		try {
			nextContext.getRuntime().init();
		} 
		catch (ArooaException e) {
			variables.getArooaContext().getConfigurationNode().removeChild(i);
		}
	}
}
