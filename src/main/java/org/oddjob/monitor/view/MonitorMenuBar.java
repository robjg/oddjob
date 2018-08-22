package org.oddjob.monitor.view;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.oddjob.arooa.design.actions.ConfigurableMenus;
import org.oddjob.arooa.design.designer.PopupMenuProvider;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.DetailModel;
import org.oddjob.monitor.model.SelectedContextAware;

/**
 * Implementation of the monitor menu bar.
 * 
 * @author Rob Gordon 
 */

public class MonitorMenuBar extends JMenuBar implements PopupMenuProvider {
	private static final long serialVersionUID = 2011101000L;

	public static final String JOB_MENU_ID = "Job";
	
	/** The file menu. */
	private final JMenu fileMenu;

	private JMenu[] lastFormMenus;

	private JPopupMenu popupMenu;
	
	private MenuSelection selectionListener;
	
	/** Explorer model for job actions to observe. */
	private DetailModel detailModel;
	
	/**
	 * Constructor.
	 *
	 */
	public MonitorMenuBar() {
		
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
				
		this.add(fileMenu);
	}
	
	public JMenu getFileMenu() {
		return fileMenu;
	}
		
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	/**
	 * Associate the menu with an explorer session.
	 * 
	 * @param model DetailModel.
	 */
	public void setSession(final ExplorerJobActions jobActions, DetailModel model) {
		if (detailModel != null) {
			throw new IllegalStateException(
					"noSession() should have been called first.");
		}
		
		detailModel = model;

		ConfigurableMenus formMenus = new ConfigurableMenus();
		
		final ExplorerEditActions editActions = 
			new ExplorerEditActions();
		
		editActions.contributeTo(formMenus);

		jobActions.contributeTo(formMenus);
		
		selectionListener = new MenuSelection(
				new SelectedContextAware() {
			public void setSelectedContext(ExplorerContext context) {
				editActions.setSelectedContext(context);
				jobActions.setSelectedContext(context);
			}
			
			@Override
			public void prepare() {
				editActions.prepare();
				jobActions.prepare();
			}
		});
		
		detailModel.addPropertyChangeListener(
				selectionListener);

		lastFormMenus = formMenus.getJMenuBar();
		
		lastFormMenus[0].addMenuListener(selectionListener);
		lastFormMenus[1].addMenuListener(selectionListener);
		
		popupMenu = formMenus.getPopupMenu();
		
		popupMenu.addPopupMenuListener(selectionListener);

		for (JMenu menu : lastFormMenus) {
			this.add(menu);
		}
		
		this.validate();
		this.repaint();
	}
	
	/**
	 * Remove any association with an Explorer session.
	 *
	 */
	public void noSession() {
		
		if (detailModel != null) {
			detailModel.removePropertyChangeListener(
					selectionListener);
			detailModel = null;
		}
		
		selectionListener = null;

		if (lastFormMenus != null) {
			for (JMenu menu : lastFormMenus) {
				this.remove(menu);
			}
			lastFormMenus = null;
			this.validate();
			this.repaint();
		}
	}
	
	/**
	 * Internal class to handle menu selection.
	 */
	class MenuSelection implements PropertyChangeListener, PopupMenuListener, MenuListener {
		
		/** Job Actions for the job menu. */
		private SelectedContextAware selectionAware;
		
		/** Track ExplorerContext as the selected job changes. */
		private ExplorerContext context;

		MenuSelection(SelectedContextAware jobActions) {
			this.selectionAware = jobActions;
		}
		
		public void propertyChange(PropertyChangeEvent evt) {
			if (DetailModel.SELECTED_CONTEXT_PROPERTY.equals(
					evt.getPropertyName())) {
				context = (ExplorerContext) evt.getNewValue();
				selectionAware.setSelectedContext(context);
			}
		}
		
		public void menuSelected(MenuEvent e) {
			menuSelect();
		}
		
		public void menuCanceled(MenuEvent e) {
		}
		
		public void menuDeselected(MenuEvent e) {
		}
		
		public void popupMenuCanceled(PopupMenuEvent e) {
		}
		
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		}
		
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			menuSelect();
		}
		
		void menuSelect() {
			if (context == null) {
				// Will this ever happen?
				return;
			}
			selectionAware.prepare();
		}
	}
	
}
