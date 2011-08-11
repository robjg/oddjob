package org.oddjob.monitor;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobServices;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.design.view.ScreenPresence;
import org.oddjob.arooa.design.view.Standards;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ConfigOwnerEvent;
import org.oddjob.arooa.parsing.ConfigSessionEvent;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.ElementConfiguration;
import org.oddjob.arooa.parsing.OwnerStateListener;
import org.oddjob.arooa.parsing.SessionStateListener;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.framework.SerializableJob;
import org.oddjob.monitor.context.AncestorSearch;
import org.oddjob.monitor.control.PropertyPolling;
import org.oddjob.monitor.model.ConfigContextInialiser;
import org.oddjob.monitor.model.ExplorerModel;
import org.oddjob.monitor.model.ExplorerModelImpl;
import org.oddjob.monitor.model.JobTreeNode;
import org.oddjob.monitor.view.ExplorerComponent;
import org.oddjob.monitor.view.MonitorMenuBar;
import org.oddjob.state.JobState;
import org.oddjob.swing.SwingInputHandler;
import org.oddjob.util.SimpleThreadManager;
import org.oddjob.util.ThreadManager;

/**
 * @oddjob.description Runs Oddjob Explorer. This is the default job that 
 * Oddjob runs on startup.
 * <p>
 * In the log panel the log level shown is set to be that of the 
 * rootLogger in the log4j.properties file in the <code>opt/classes</code> 
 * directory. By default it is set to INFO so you will not see 
 * DEBUG messages in the log panel. For more information on configuring the 
 * file see <a hre="http://logging.apache.org/log4j">
 * http://logging.apache.org/log4j</a>
 * 
 * @author Rob Gordon
 */

public class OddjobExplorer extends SerializableJob
implements Stoppable {
	
    private static final long serialVersionUID = 2011042800L;

    private static final Logger logger = Logger.getLogger(OddjobExplorer.class);

    public static final String ODDJOB_PROPERTY ="oddjob";
    
    public static final String DEFAULT_TITLE = "Oddjob Explorer";
    
    protected transient VetoableChangeSupport vetoableChangeSupport;
    
    /** 
     * @oddjob.property
     * @oddjob.description The directory the file chooser 
     * should use when opening and saving Oddjobs.
     * @oddjob.required No. 
     */
	private File dir;

    
    /**
     * @oddjob.property
     * @oddjob.description The root node of jobs to monitor.
     * @oddjob.required No.
     */
	private transient Oddjob oddjob;

	private transient ConfigurationSession focus;
	
    /**
     * @oddjob.property
     * @oddjob.description How often to poll in milli seconds for property updates.
     * @oddjob.required No.
     */
	private transient long pollingInterval;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The log format for formatting log messages. For more
	 * information on the format please see <a href="http://logging.apache.org/log4j/docs/">
	 * http://logging.apache.org/log4j/docs/</a>
	 * @oddjob.required No.
	 */
	private transient String logFormat;
	
	/** The frame */
	private volatile transient JFrame frame;
		
	private transient Action newAction;
	private transient Action openAction;
	private transient Action saveAction;
	private transient Action saveAsAction;
	private transient Action closeAction;
	private transient Action exitAction;

	private transient JMenu fileMenu;
	
	private transient ExplorerModel explorerModel;	
	private transient ExplorerComponent explorerComponent;	
	private transient MonitorMenuBar menuBar;
	
	/** Poll for property changes. */
	private transient PropertyPolling propertyPolling;
	
	/**
	 * The ThreadManager. This job will not
	 * stop until the ThreadManager says it can.
	 */	
	private transient ThreadManager threadManager;

	/**
	 * @oddjob.property
	 * @oddjob.description Internal services. Set automatically
	 * by Oddjob.
	 * @oddjob.required No.
	 */
	private transient OddjobServices oddjobServices;
	
	private static List<File> sharedFileHistory;
	private List<File> fileHistory;

	// These will be serialzed so frame settings are preserved.
	private ScreenPresence screen;
	
	/** Used to track modification changes. */
	transient private Set<ConfigurationOwner> owners;
		
	public OddjobExplorer() {
		completeConstruction();
		
		ScreenPresence whole = ScreenPresence.wholeScreen();
		screen = whole.smaller(0.66);
	}
	
	/**
	 * Completes construction of this object. Required because de-serialisation
	 * doesn't go through the constructor.
	 */
	private void completeConstruction() {
		
		if (fileHistory == null) {
			fileHistory = new ArrayList<File>();
		}
		
		vetoableChangeSupport = 
			new VetoableChangeSupport(this);
		owners = new LinkedHashSet<ConfigurationOwner>();
		 
		newAction = new NewAction();
		openAction = new OpenAction();
		saveAction = new SaveAction();
		saveAsAction = new SaveAsAction();
		closeAction = new CloseAction();
		exitAction = new ExitAction();
		
		pollingInterval = 5000;
		
		if (sharedFileHistory == null) {
			// first to start
			sharedFileHistory = fileHistory; 
		}
		else {
			if (fileHistory.size() > sharedFileHistory.size()) {
				sharedFileHistory = fileHistory;
			}
			else {
				fileHistory = sharedFileHistory;
			}
		}
		propertyPolling = new PropertyPolling(this);		
	}

	protected ExplorerComponent getExplorerComponent() {
		return explorerComponent;
	}
	
	@Inject
	public void setOddjobServices(OddjobServices oddjobServices) {
		this.oddjobServices = oddjobServices;
	}
	
	public void setOddjob(Oddjob oddjob) throws PropertyVetoException {
		if (this.oddjob == oddjob) {
			return;
		}
		
		Object oldValue = this.oddjob;
		
		vetoableChangeSupport.fireVetoableChange(ODDJOB_PROPERTY, oldValue, oddjob);
		
		if (this.oddjob != null) {
			addFileHistory(this.oddjob.getFile());
			this.oddjob.destroy();
		}
		
		this.oddjob = oddjob;
				
		if (oddjob != null && oddjob.getDir() != null) {
			this.dir = oddjob.getDir();			
		}
		
		firePropertyChange(ODDJOB_PROPERTY, oldValue, this.oddjob);
	}
		
	public Oddjob getOddjob() {
		return oddjob;
	}
	
	@ArooaAttribute
	public void setDir(File dir) {
		this.dir = dir;
	}

	public File getDir() {
		return dir;
	}

	/**
	 * @oddjob.property title
	 * @oddjob.description The Explorer frame's title.

	 * @return The title.
	 */
	public String getTitle() {
		return frame.getTitle();
	}
	
	void addFileHistory(File file) {
		if (file == null) {
			return;
		}
		
		fileHistory = sharedFileHistory;
		fileHistory.remove(file);
		fileHistory.add(file);
		while (fileHistory.size() > 4) {
			fileHistory.remove(0);
		}
		sharedFileHistory = fileHistory;
		updateFileMenu();
	}

	void updateFileMenu() {
		
		fileMenu.removeAll();
		fileMenu.add(new JMenuItem(newAction));
		fileMenu.add(new JMenuItem(openAction));
		fileMenu.add(new JMenuItem(saveAction));
		fileMenu.add(new JMenuItem(saveAsAction));
		fileMenu.add(new JMenuItem(closeAction));
		fileMenu.add(new JSeparator());
		
		Action a[] = new Action[fileHistory.size()];
		// reverse for recent first
		for (int i = 0; i < fileHistory.size(); ++i) {
			a[fileHistory.size() - i - 1] = new HistoryAction(fileHistory.size() - i, (File) fileHistory.get(i));
		}
		boolean hasHistory = false;
		for (int i = 0; i < a.length; ++i) {
			hasHistory = true;
			fileMenu.add(new JMenuItem(a[i]));
		}
		if (hasHistory) {
			fileMenu.add(new JSeparator());
		}
		fileMenu.add(new JMenuItem(exitAction));
	}
	
	/**
	 */
	public void show() {
		if (frame == null) {
			throw new IllegalStateException("No frame - explorer must have stopped.");
		}
		frame.toFront();
	}
	
	class CheckOddjobStopped implements VetoableChangeListener {
		
		public void vetoableChange(PropertyChangeEvent evt)
				throws PropertyVetoException {
			
			if (!ODDJOB_PROPERTY.equals(evt.getPropertyName())) {
				return;
			}
			
			Oddjob oddjob = (Oddjob) evt.getOldValue();
			
			if (oddjob == null) {
				return;
			}
			
			if (oddjob.lastStateEvent().getState() == JobState.EXECUTING) {
				throw new PropertyVetoException("Oddjob Not Stopped.", evt);
			}
			
			String[] active = threadManager.activeDescriptions();
			if (active.length > 0) {
				StringBuilder message = new StringBuilder();
				message.append("The following are still running:\n\n");
				for (int i = 0; i < active.length; ++i) {
					message.append(active[i]);
					message.append('\n');
				}
				message.append('\n');
				
				JOptionPane.showMessageDialog(frame, message, "Jobs Running!", JOptionPane.ERROR_MESSAGE);
				throw new PropertyVetoException(message.toString(), evt);
			}
		}
	}

	class TrackConfigurationOwners
	implements TreeModelListener {
		
		public void treeNodesChanged(TreeModelEvent e) {
		}
		
		public void treeStructureChanged(TreeModelEvent e) {
		}
		
		public void treeNodesInserted(TreeModelEvent event) {
		    for (Object child: event.getChildren()) {
				Object component = ((JobTreeNode) child).getComponent();
				if (component instanceof ConfigurationOwner) {
					owners.add((ConfigurationOwner) component); 
				}
		    }
		}
		
		public void treeNodesRemoved(TreeModelEvent event) {
			for (Object child : event.getChildren()) {
				Object component = ((JobTreeNode) child).getComponent();
				owners.remove(component);
			}
		}
	}
	
	/** Don't want to notify of changes during save as action. */
	private boolean saveAs = false;
	
	/**
	 * Track ConfigurationOwners. Used to check we can stop.
	 */
	class CheckConfigurationsSaved
	implements 
			VetoableChangeListener {
			
		public void vetoableChange(PropertyChangeEvent evt)
		throws PropertyVetoException {
	
			if (!ODDJOB_PROPERTY.equals(evt.getPropertyName())) {
				return;
			}
			
			Oddjob oldOddjob = (Oddjob) evt.getOldValue();
			
			if (oldOddjob != null) {
				List<ConfigurationOwner> modified = new ArrayList<ConfigurationOwner>();
				for (ConfigurationOwner owner : owners) {
					
					if (saveAs && owner == oldOddjob) {
						continue;
					}
					
					ConfigurationSession session = owner.provideConfigurationSession();
					if (session != null && session.isModified()) {
						modified.add(owner);
					}
				}
				if (!modified.isEmpty() && !canClose(modified)) {
					throw new PropertyVetoException("Outstanding Modifications", evt);
				}
			}
			
			owners.clear();

			Oddjob newOddjob = (Oddjob) evt.getNewValue();
			if (newOddjob != null) {
				owners.add(newOddjob);
			}
		}
		
		boolean canClose(Collection<ConfigurationOwner> modified) {
			StringBuilder message = new StringBuilder();
			message.append("Unsaved modifications! Continue?\n\n");
			for (ConfigurationOwner owner : modified) {
				message.append(owner.toString());
				message.append('\n');
			}
			message.append('\n');

			int option = JOptionPane.showConfirmDialog(
					explorerComponent, 
					message.toString(), 
					"Unsaved Modifications",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			
			if (option == JOptionPane.OK_OPTION) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	// This is a bit of a bodge because the ChangeFocus class is created
	// twice so can't contain state.
	
	private transient String name;
	
	private transient boolean modified;
	
	private transient ConfigurationOwner current;
	
	/**
	 * Track current configuration. Update the title.
	 */
	class ChangeFocus 
	implements 
			PropertyChangeListener, 
			TreeSelectionListener, 
			OwnerStateListener,
			SessionStateListener {
			
		public void propertyChange(PropertyChangeEvent evt) {
			
			if (!ODDJOB_PROPERTY.equals(evt.getPropertyName())) {
				return;
			}
			
			Oddjob newJob = (Oddjob) evt.getNewValue();
			
			setOwner(newJob);
		}
		
		public void valueChanged(TreeSelectionEvent e) {
			// tried: (JobTreeNode) e.getPath().getLastPathComponent();
			// but it's not null id de-selection.

			JobTreeNode selected = 
				(JobTreeNode) ((JTree) e.getSource()).getLastSelectedPathComponent();
			
			if (selected == null) {
				setOwner(oddjob);
			}
			else {
				AncestorSearch search = new AncestorSearch(
						selected.getExplorerContext());
				
				ConfigurationOwner configOwner = 
					(ConfigurationOwner) search.getValue(
							ConfigContextInialiser.CONFIG_OWNER);

				setOwner(configOwner);
			}
		}

		public void sessionChanged(ConfigOwnerEvent event) {
			updateSession(event.getSource().provideConfigurationSession());
			writeTitle();
		}

		public void sessionModifed(ConfigSessionEvent event) {
			modified = true; 
			writeTitle();
		}

		public void sessionSaved(ConfigSessionEvent event) {
			modified = false; 
			writeTitle();
		}
		
		void setOwner(ConfigurationOwner owner) {
			
			if (current == owner) {
				return;
			}
			
			if (current != null) {
				current.removeOwnerStateListener(this);
			}
			
			if (owner == null) {
				name = null;
				modified = false;
				updateSession(null);
			}
			else {
				name = owner.toString();
				updateSession(owner.provideConfigurationSession());				
				owner.addOwnerStateListener(this);
			}
			
			current = owner;
			
			writeTitle();
		}
		
		void updateSession(ConfigurationSession session) {
			if (focus != null) {
				focus.removeSessionStateListener(this);
			}
			focus = session;
			
			if (focus == null) {
				modified = false;
			}
			else {
				focus.addSessionStateListener(this);
				modified = focus.isModified();
			}
		}
		
		void writeTitle() {
			
			if (frame == null) {
				return;
			}

			String title = DEFAULT_TITLE;
			
			if (name != null) {
				
				title += " - " + name + (modified ? " *" : "");
			}
			
			frame.setTitle(title);
		}
	}
	
	class ChangeView implements PropertyChangeListener {
		
		public void propertyChange(PropertyChangeEvent evt) {
			
			if (!ODDJOB_PROPERTY.equals(evt.getPropertyName())) {
				return;
			}
			
			Oddjob oldJob = (Oddjob) evt.getOldValue();
			Oddjob newJob = (Oddjob) evt.getNewValue();
			
			if (oldJob != null) {
				
				menuBar.noSession();
				explorerComponent.destroy();
				explorerModel.destroy();
				
				frame.getContentPane().removeAll();
			}
			
			if (newJob != null) {
				
				ExplorerModelImpl explorerModel = new ExplorerModelImpl(
						new StandardArooaSession());
				explorerModel.setThreadManager(threadManager);
				explorerModel.setLogFormat(logFormat);
				explorerModel.setOddjob(newJob);
				OddjobExplorer.this.explorerModel = explorerModel;
				
			    explorerComponent = new ExplorerComponent(
			    		explorerModel, 
			    		propertyPolling);	
			    
			    explorerComponent.bindTo(menuBar);
			    
			    JTree tree = explorerComponent.getTree();
			    tree.addTreeSelectionListener(new ChangeFocus());
			    tree.getModel().addTreeModelListener(new TrackConfigurationOwners());
			    
				frame.getContentPane().add(explorerComponent);
//				frame.pack();
				
				explorerComponent.balance();
			}
			
			frame.validate();
			frame.repaint();
		}
	}
	
	/**
	 * Helper method to create the menu bar.
	 *
	 */
	void createView() {
		menuBar = new MonitorMenuBar();
		fileMenu = menuBar.getFileMenu();
		
		updateFileMenu();					
		
		frame = new JFrame();
		screen.fit(frame);
		 
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				maybeCloseWindow();
			}

			public void windowClosed(WindowEvent e) {
			}
		});

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		frame.setTitle(DEFAULT_TITLE);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {

		threadManager = new SimpleThreadManager();
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		createView();
		
		// In case Explorer was started with an Oddjob.
		Oddjob oddjob = this.oddjob;
		this.oddjob = null;
		
		VetoableChangeListener checkStop = new CheckOddjobStopped();
		VetoableChangeListener checkSaved = new CheckConfigurationsSaved();
		// Order important otherwise we see modified indicator on destroy.
		PropertyChangeListener changeTitle = new ChangeFocus();
		PropertyChangeListener changeView = new ChangeView();
		
		vetoableChangeSupport.addVetoableChangeListener(checkStop);
		vetoableChangeSupport.addVetoableChangeListener(checkSaved);
		addPropertyChangeListener(changeView);
		addPropertyChangeListener(changeTitle);

		setOddjob(oddjob);
		
		frame.setVisible(true);
				
		while (!stop) {
			try {
				propertyPolling.poll();
			}
			catch (RuntimeException e) {
				logger().error("Property polling failed.", e);
			}
			synchronized (this) {
				try {
					wait(pollingInterval);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
		vetoableChangeSupport.removeVetoableChangeListener(checkStop);
		vetoableChangeSupport.removeVetoableChangeListener(checkSaved);
		removePropertyChangeListener(changeView);
		removePropertyChangeListener(changeTitle);
		
		screen = new ScreenPresence(frame);		
		
		frame = null;
		threadManager.close();
		
		return 0;	
	}
	
	/**
	 * Close.
	 * @throws PropertyVetoException 
	 */
	private void maybeCloseWindow() {

		try {
			setOddjob(null);
		} catch (PropertyVetoException e) {
			// Can't close...
			return;
		}
		
		frame.dispose();
		stop = true;
		synchronized (OddjobExplorer.this) {
			OddjobExplorer.this.notifyAll();
		}
		logger().debug("Monitor closed.");
	}

	public void onStop() throws FailedToStopException {
		if (oddjob != null) {
			oddjob.stop();
		}
		maybeCloseWindow();
	}
	
	private Oddjob newOddjob() {

		Oddjob oddjob = new Oddjob();
		oddjob.setArooaSession(getArooaSession());
		oddjob.setOddjobServices(oddjobServices);
		oddjob.setInputHandler(new SwingInputHandler(frame));
		return oddjob;
	}
	
	class NewAction extends AbstractAction {
		private static final long serialVersionUID = 2008120400;
		
		NewAction() {
			putValue(Action.NAME, "New");
			putValue(Action.MNEMONIC_KEY, Standards.NEW_MNEMONIC_KEY); 
			putValue(Action.ACCELERATOR_KEY, Standards.NEW_ACCELERATOR_KEY);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(final ActionEvent e) {
			
			try {
				Oddjob newJob = newOddjob();							
				
				ArooaConfiguration saveAsConfig = new ArooaConfiguration() {
					public ConfigurationHandle parse(ArooaContext parentContext)
							throws ArooaParseException {
						
						final ConfigurationHandle handle = new ElementConfiguration(
								Oddjob.ODDJOB_ELEMENT).parse(parentContext);
						
						return new ConfigurationHandle() {
							public ArooaContext getDocumentContext() {
								return handle.getDocumentContext();
							};
							public void save() throws ArooaParseException {
								saveAsAction.actionPerformed(e);
							};
						};
					}
				};
				
				
				newJob.setConfiguration(saveAsConfig);
	
				newJob.load();
				
				setOddjob(newJob);
			}
			catch (PropertyVetoException e1) {
				// Ignore;
			}
			catch (RuntimeException ex) {
				logger().warn("Exception creating new Oddjob.", ex);
				JOptionPane.showMessageDialog(frame, ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	class SaveAction extends AbstractAction {
		private static final long serialVersionUID = 2008111300;
	
		SaveAction() {
			putValue(Action.NAME, "Save");
			putValue(Action.MNEMONIC_KEY, Standards.SAVE_MNEMONIC_KEY); 
			putValue(Action.ACCELERATOR_KEY, Standards.SAVE_ACCELERATOR_KEY);
			
			OddjobExplorer.this.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					setEnabled(oddjob != null);
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
	
			if (focus == null) {
				return;
			}
			
			try {
				focus.save();
			}
			catch (Exception exception) {
				logger.error("Failed creating Design from XML.", exception);

				JOptionPane.showMessageDialog(
						frame.getContentPane(), 
						exception.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);

				return;
			}
		}
	}

	
	class SaveAsAction extends AbstractAction {
		private static final long serialVersionUID = 2008111300;
		
		SaveAsAction() {
			putValue(Action.NAME, "Save As...");
			putValue(Action.MNEMONIC_KEY, Standards.SAVEAS_MNEMONIC_KEY); 
			putValue(Action.ACCELERATOR_KEY, Standards.SAVEAS_ACCELERATOR_KEY);
			
			OddjobExplorer.this.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					setEnabled(oddjob != null);
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			
			ConfigurationSession config = oddjob.provideConfigurationSession();
			
			if (config == null) {
				
				JOptionPane.showMessageDialog(
						frame.getContentPane(), 
						"No Configuration loaded - Run Oddjob first.",
						"No Config",
						JOptionPane.INFORMATION_MESSAGE);
					
				return;
			}
			
			JFileChooser chooser = new JFileChooser();
			if (dir != null) {
				chooser.setCurrentDirectory(dir);
			}
			int option = chooser.showSaveDialog(frame);
			if (option != JFileChooser.APPROVE_OPTION) {
				return;
			}

			File file = chooser.getSelectedFile(); 

			try {
							
				XMLArooaParser parser = new XMLArooaParser();
				
				ArooaConfiguration oddjobConfiguration = 
					config.dragPointFor(oddjob);
				
				parser.parse(
						oddjobConfiguration);

				PrintWriter printWriter = new PrintWriter(new FileWriter(file));
				
				printWriter.print(parser.getXml());

				printWriter.close();
				
				addFileHistory(oddjob.getFile());
				
				Oddjob newJob = newOddjob();
				newJob.setFile(file);

				newJob.load();
				
				saveAs = true;
				setOddjob(newJob);
			}
			catch (PropertyVetoException e1) {
				// Ignore;
			}
			catch (Exception exception) {
				logger.error("Failed creating Design from XML.", exception);

				JOptionPane.showMessageDialog(
						frame.getContentPane(), 
						exception.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);

				return;
			}
			finally {
				saveAs = false;
			}
		}
	}

	class OpenAction extends AbstractAction {
		private static final long serialVersionUID = 2008120400;
		
		OpenAction() {
			putValue(Action.NAME, "Open");
			putValue(Action.MNEMONIC_KEY, Standards.OPEN_MNEMONIC_KEY); 
			putValue(Action.ACCELERATOR_KEY, Standards.OPEN_ACCELERATOR_KEY);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			if (dir != null) {
				chooser.setCurrentDirectory(dir);
			}
			
			int option = chooser.showOpenDialog(frame);
			if (option != JFileChooser.APPROVE_OPTION) {
				return;
			}
			
			File file = chooser.getSelectedFile();
			
			
			try {
	
				Oddjob newJob = newOddjob();
				
				newJob.setFile(file);
				
				newJob.load();
				
				setOddjob(newJob);
			}
			catch (PropertyVetoException e1) {
				// Ignore;
			}
			catch (RuntimeException ex) {
				logger().warn("Exception opening file [" + file + "]", ex);
				JOptionPane.showMessageDialog(frame, ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	class CloseAction extends AbstractAction {
		private static final long serialVersionUID = 2008120400;
		
		CloseAction() {
			putValue(Action.NAME, "Close");
			putValue(Action.MNEMONIC_KEY, Standards.CLOSE_MNEMONIC_KEY); 
			putValue(Action.ACCELERATOR_KEY, Standards.CLOSE_ACCELERATOR_KEY);
			
			OddjobExplorer.this.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					setEnabled(oddjob != null);
				}
			});
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				setOddjob(null);
			} catch (PropertyVetoException e1) {
				// Ignore;
			}
			catch (RuntimeException ex) {
				logger().warn("Exception closing Oddjob.", ex);
				JOptionPane.showMessageDialog(frame, ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}	
	
	class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 2008120400;
		
		ExitAction() {
			putValue(Action.NAME, "Exit");
			putValue(Action.MNEMONIC_KEY, Standards.EXIT_MNEMONIC_KEY); 
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			maybeCloseWindow();
		}
	}

	class HistoryAction extends AbstractAction {
		private static final long serialVersionUID = 2008120400;
		
		private final File file;

		HistoryAction(int number, File file) {
			putValue(Action.NAME, "" + number + " " + file.getName() + " ["
					+ file.getAbsoluteFile().getParent() + "]");
			putValue(Action.MNEMONIC_KEY, new Integer(48 + number));
			this.file = file;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				Oddjob newJob = newOddjob();				
				newJob.setFile(file);
				
				newJob.load();
				
				setOddjob(newJob);
			}
			catch (PropertyVetoException e1) {
				// Ignore;
			}
			catch (RuntimeException ex) {
				logger().warn("Exception opening file [" + file + "]", ex);
				JOptionPane.showMessageDialog(frame, ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Custom serialsation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}

	/**
	 * @return Returns the pollingInterval.
	 */
	public synchronized long getPollingInterval() {
		return pollingInterval;
	}

	/**
	 * @param pollingInterval
	 *            The pollingInterval to set.
	 */
	public synchronized void setPollingInterval(long pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

}
