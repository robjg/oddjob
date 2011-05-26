package org.oddjob.monitor.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * Model for a table to display properties.
 * 
 * @author Rob Gordon 
 */

public class PropertyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 20051109;
	
    /** The keys */
    private List<String> keys = new ArrayList<String>();
    
	/** The props. */
	private List<String> values = new ArrayList<String>();
		
	private final String colNames[] = { "Name", "Value" };

	public void update(Map<String, String> props) {				
		keys = new ArrayList<String>(props.keySet());
		values = new ArrayList<String>(props.values());
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return keys.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 2;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int columnIndex) {
		return colNames[columnIndex];
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return keys.get(rowIndex);
		}
		else {
			return values.get(rowIndex);
		}
	}
}
