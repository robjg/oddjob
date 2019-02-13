/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.design.view.SwingItemFactory;
import org.oddjob.arooa.design.view.SwingItemView;
import org.oddjob.arooa.design.view.multitype.AbstractMultiTypeModel;
import org.oddjob.arooa.design.view.multitype.EditableValue;
import org.oddjob.arooa.design.view.multitype.MultiTypeRow;
import org.oddjob.arooa.design.view.multitype.MultiTypeStrategy;
import org.oddjob.arooa.design.view.multitype.MultiTypeTableWidget;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.AbstractConfigurationNode;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ArooaHandler;
import org.oddjob.arooa.parsing.CutAndPasteSupport;
import org.oddjob.arooa.parsing.PrefixMappings;
import org.oddjob.arooa.parsing.QTag;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.runtime.AbstractRuntimeConfiguration;
import org.oddjob.arooa.runtime.ConfigurationNode;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.values.VariablesJob;

/**
 * A {@link DesignFactory} for {@link VariablesJob}
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
	
	void addProperty(int index, VariablesDesignProperty property) {
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
	
	VariablesDesignProperty propertyAt(int index) {
		return properties.get(index).getProperty();
	}
	
	int propertyCount() {
		return properties.size();
	}
}

/**
 * Keeps track of the property and its instance.
 */
class PropertyValuePair { 

	private final VariablesDesignProperty property;
	
	private DesignInstance value;
	
	PropertyValuePair(VariablesDesignProperty property) {
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

	VariablesDesignProperty getProperty() {
		return property;
	}
	
	DesignInstance getValue() {
		return value;
	}
	
}
	
/**
 * Receive notifications of when variables are added and removed.
 */
interface VariablesListener {
	
	void variableAdded(int index);
	
	void variableRemoved(int index);
}


/**
 * 
 */
class VariablesDesignProperty extends SimpleDesignProperty {
	
	private String property;
	
	public VariablesDesignProperty(String property, 
			Class<?> propertyClass,
			ArooaType type, DesignInstance parent) {
		super(null, propertyClass, type, parent);
		this.property = property;
	}
	
	@Override
	public String property() {
		return property;
	}
	
	void changePropertyName(String name) {
		this.property = name;
	}
}

/**
 * Handles parsing of the design etc.
 */
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
				variables.addProperty(index, (VariablesDesignProperty) value);
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

				if (element.getAttributes().getAttributeNames().length > 0) {
					throw new ArooaException("Property can't contain attributes: " + 
							element.getAttributes().getAttributeNames()[0]);
				}
				
				VariablesDesignProperty property = new VariablesDesignProperty(
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


class VariablesModel extends AbstractMultiTypeModel {

	public static final QTag NULL_TAG = new QTag("");
	
	private final QTag[] supportedTypes; 
	
	private final List<VariableRow> variableRows = 
			new ArrayList<VariableRow>();
	
	private final VariablesDesign variables;
	
	public VariablesModel(VariablesGrid variablesGrid) {

		this.variables = variablesGrid.getVariables();

		variables.addVariablesListener(new VariablesListener() {
			@Override
			public void variableAdded(final int index) {				
				final VariableRow variableRow = new VariableRow(index);
				variableRows.add(index, variableRow);
				fireRowInserted(index);
				variables.propertyAt(index).addDesignListener(new DesignListener() {
					public void childAdded(DesignStructureEvent event) {
						variableRow.setInstance(event.getChild());
						fireRowChanged(index);
					}
					public void childRemoved(DesignStructureEvent event) {
						variableRow.setInstance(null);
						fireRowChanged(index);
					}
				});
				
			}
			public void variableRemoved(int index) {
				variableRows.remove(index);
				fireRowRemoved(index);
			}			
		});		

		ArooaContext context = variables.getArooaContext(); 
		
		ElementMappings mappings = context.getSession(
				).getArooaDescriptor().getElementMappings();
		
		ArooaSession session = context.getSession();
		
		InstantiationContext instantiationContext = 
			new InstantiationContext(ArooaType.VALUE, 
					new SimpleArooaClass(ArooaValue.class),
					session.getArooaDescriptor().getClassResolver(),
					session.getTools().getArooaConverter());
		
		ArooaElement[] supportedElements = 
			mappings.elementsFor(instantiationContext);
				
		TreeSet<QTag> sortedTypes = new TreeSet<QTag>();
		
		for (ArooaElement element : supportedElements) {
			sortedTypes.add(new QTag(
					element, context));
		}
		
		this.supportedTypes = sortedTypes.toArray(new QTag[sortedTypes.size()]);
	}
	
	
	@Override
	public Object getDeleteOption() {
		return NULL_TAG;
	}

	@Override
	public Object[] getTypeOptions() {
		return supportedTypes;
	}
	
	@Override
	public void createRow(Object creator, int row) {
		insertProperty(row, (String) creator);
	}
	
	@Override
	public void swapRow(int from, int direction) {
		ArooaContext propertyContext = variables.propertyAt(from).getArooaContext();
		ArooaContext parentContext = propertyContext.getParent();
		
		CutAndPasteSupport.cut(parentContext, propertyContext);
		
		int to = from+direction;
		
		try {
			CutAndPasteSupport.paste(parentContext, to, 
					propertyContext.getConfigurationNode());
		} catch (ArooaParseException e) {
			throw new DesignViewException(e);
		}
	}
	
	@Override
	public void removeRow(int index) {
		removeProperty(variables.propertyAt(index));
	}
	
	@Override
	public MultiTypeRow getRow(int index) {
		return variableRows.get(index);
	}
	
	@Override
	public int getRowCount() {
		return variableRows.size();
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
	
	/**
	 * 
	 * @param index
	 * @param property
	 */
	void insertProperty(int index, String property) {
		
		variables.getArooaContext().getConfigurationNode().setInsertPosition(index);
		
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
	
	/**
	 * 
	 */
	class VariableRow implements MultiTypeRow {
		
		private final VariablesDesignProperty designProperty;
		
		private DesignInstance instance;
		
		private Component component;
		
		public VariableRow(int index) {
			this.designProperty = variables.propertyAt(index);
		}
		
		void setInstance(DesignInstance instance) {
			this.instance = instance;
			if (instance == null) {
				component = null;
			}
			else {
				Form designDefintion = instance.detail();
				this.component = SwingFormFactory.create(designDefintion).cell();
			}
		}
		
		@Override
		public String getName() {
			return designProperty.property();
		}

		@Override
		public void setName(String name) {
			designProperty.changePropertyName(name);
		}
		
		@Override
		public void setType(Object value) {
			
			InstanceSupport support = new InstanceSupport(designProperty);
				
			QTag newType = (QTag) value;
										
			QTag oldType;
			if (instance == null) {
				oldType = NULL_TAG;
			}
			else {
				oldType = InstanceSupport.tagFor(instance);
			}
			
			if (newType.equals(oldType)) {
				// if the type hasn't changed then nothing to do.
				return;
			}
			else if (!NULL_TAG.equals(oldType)){
				support.removeInstance(instance);
			}
					
			if (NULL_TAG.equals(newType)) {
				return;
			}
			try {
				support.insertTag(0, newType);
			}
			catch (ArooaParseException e) {
				throw new DesignViewException(e);
			}
		}
		
		@Override
		public Object getType() {
			if (instance != null) {
				return InstanceSupport.tagFor(instance);
			}
			else {
				return NULL_TAG;
			}
		}
		
		@Override
		public EditableValue getValue() {
			if (instance == null) {
				return null;
			}
			else {
				return new EditableValue() {
					@Override
					public Component getEditor() {
						return component;
					}
					
					@Override
					public void commit() {
					}
					
					@Override
					public void abort() {
					}
				};
			}
		}		
	}
}

class VariablesTableView implements SwingItemView {

	private Component component;
		
	public VariablesTableView(VariablesGrid variablesGrid) {
		
		this.component = new MultiTypeTableWidget(
				new VariablesModel(variablesGrid), 
				MultiTypeStrategy.Strategies.NAMED);
	}

	/* (non-Javadoc)
	 * @see org.oddjob.designer.view.ViewProducer#inline(java.awt.Container, int, int, boolean)
	 */
	public int inline(Container container, int row, int column,
			boolean selectionInGroup) {
		
		GridBagConstraints c = new GridBagConstraints();
		
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

}
