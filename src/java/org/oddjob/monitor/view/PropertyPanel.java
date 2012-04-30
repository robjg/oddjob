/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.oddjob.monitor.model.PropertyModel;

/**
 *  The Tab Panel for properties.
 *  
 *  @author Rob Gordon.
 */
public class PropertyPanel extends JPanel 
implements Observer {
	private static final long serialVersionUID = 0;
	
	/** Rough guess at number of characters screen width, to limit
	 * the length of tool tips. */
	private int screenWidth = (int)
			java.awt.Toolkit.getDefaultToolkit().getScreenSize(
					).getWidth() / 12;

	
	private PropertyTableModel  tableModel;
	
	/**
	 * Constructor.
	 * 
	 * @param propertyModel
	 */
	public PropertyPanel(PropertyModel propertyModel) {
		propertyModel.addObserver(this);

		tableModel = new PropertyTableModel(); 
		JTable propTable = new JTable(tableModel);
		
		propTable.setCellSelectionEnabled(true);
		
		TableCellRenderer defaultRenderer = 
			propTable.getDefaultRenderer(String.class);
		
		propTable.setDefaultRenderer(Object.class, 
				new PropertyRenderer(defaultRenderer));
		
		JScrollPane formScroll = new JScrollPane();
		formScroll.setViewportView(propTable);
		
		setLayout(new BorderLayout());
		add(formScroll, BorderLayout.CENTER);
	}
	
	/**
	 * This custom renderer just sets the tool tip text
	 * to be the full text of the property.
	 *
	 */
	class PropertyRenderer 
    implements TableCellRenderer {
		
		private final TableCellRenderer defaultRenderer;
		
		public PropertyRenderer(TableCellRenderer defaultRenderer) {
			this.defaultRenderer = defaultRenderer;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			JComponent component = 
				(JComponent) defaultRenderer.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
			
			// Large Tooltip causes Windows 7 to change Graphics mode.
			// Todo: Look at using JMultiLineToolTip.
			String text = (String) value;
			if (text != null && text.length() > screenWidth) {
				text = text.substring(0, screenWidth) + "...";
			}
			component.setToolTipText(text);
			
			return component;
		}
	}
	
	/**
	 * Update from the PropertyModel.
	 * 
	 * @param o The PropertyModel.
	 * @param arg Ignored.
	 */
	public void update(Observable o, Object arg) {
		PropertyModel propertyModel = (PropertyModel) o;
		tableModel.update(propertyModel.getProperties());
	}
}
