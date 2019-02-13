package org.oddjob.monitor;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobServices;
import org.oddjob.OddjobShutdownThread;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
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
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.monitor.context.AncestorSearch;
import org.oddjob.monitor.control.PropertyPolling;
import org.oddjob.monitor.model.ConfigContextInialiser;
import org.oddjob.monitor.model.ExplorerModel;
import org.oddjob.monitor.model.ExplorerModelImpl;
import org.oddjob.monitor.model.FileHistory;
import org.oddjob.monitor.model.JobTreeNode;
import org.oddjob.monitor.view.ExplorerComponent;
import org.oddjob.monitor.view.MonitorMenuBar;
import org.oddjob.state.State;
import org.oddjob.swing.SwingInputHandler;
import org.oddjob.util.SimpleThreadManager;
import org.oddjob.util.ThreadManager;

/**
 * @oddjob.description Runs Oddjob Explorer.
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
	
    private static final long serialVersionUID = 2011101400L;

    private static final Logger logger = LoggerFactory.getLogger(OddjobExplorer.class);

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
	private transient volatile Oddjob oddjob;

	private transient ConfigurationSession focus;
	
    /**
     * @oddjob.property
     * @oddjob.description How often to poll in milli seconds for property updates.
     * @oddjob.required No.
     */
	private long pollingInterval = 5000;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The log format for formatting log messages. For more
	 * information on the format please see <a href="http://logging.apache.org/log4j/docs/">
	 * http://logging.apache.org/log4j/docs/</a>
	 * @oddjob.required No.
	 */
	private transient String logFormat;
	
    /** 
     * @oddjob.property
     * @oddjob.description A file to show when the explorer starts.
     * @oddjob.required No. 
     */
	private File file;

	/** The frame */
	private volatile transient JFrame frame;
		
	private transient Action newExplorerAction;
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
	
	private FileHistory fileHistory;

	// These will be serialzed so frame settings are preserved.
	private ScreenPresence screen;
	
	/** Used to track modification changes. */
	transient private Set<ConfigurationOwner> owners;
		
	/**
	 * Constructor to be used to create a single instance of this explorer.
	 * Typically used from code to aid debugging an Oddjob.
	 */
	public OddjobExplorer() {
		fileHistory = new FileHistory();
		
		ScreenPresence whole = ScreenPresence.wholeScreen();
		screen = whole.smaller(0.66);		
		
		completeConstruction();
	}
	
	/**
	 * Constructor when this explorer is being created as one of many.
	 * 
	 * @param controller
	 * @param screen
	 * @param sharedFileHistory
	 */
	public OddjobExplorer(MultiViewController controller, 
			ScreenPresence screen,
			FileHistory sharedFileHistory) {
		this.screen = screen;
		this.fileHistory = sharedFileHistory;
		this.newExplorerAction = new NewExplorerAction(controller);
		
		completeConstruction();
	}
	
	/**
	 * Completes construction of this object. Required because de-serialisation
	 * doesn't go through the constructor.
	 */
	private void completeConstruction() {
		
		vetoableChangeSupport = 
			new VetoableChangeSupport(this);
		owners = new LinkedHashSet<ConfigurationOwner>();
		 
		newAction = new NewAction();
		openAction = new OpenAction();
		saveAction = new SaveAction();
		saveAsAction = new SaveAsAction();
		closeAction = new CloseAction();
		exitAction = new ExitAction();
		
		fileHistory.addChangeAction(new Runnable() {
			
			@Override
			public void run() {
				if (frame == null) {
					fileHistory.removeChangeAction(this);
				}
				else {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							updateFileMenu();
						}
					});
				}
			}
		});
	}

	protected ExplorerComponent getExplorerComponent() {
		return explorerComponent;
	}
	
	/**
	 * Capture services from the containing Oddjob.
	 * 
	 * @param oddjobServices
	 */
	@Inject
	public void setOddjobServices(OddjobServices oddjobServices) {
		this.oddjobServices = oddjobServices;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.framework.BaseComponent#setArooaSession(org.oddjob.arooa.ArooaSession)
	 */
	@Override
	public void setArooaSession(ArooaSession session) {
		super.setArooaSession(session);
		
		propertyPolling = new PropertyPolling(this, session);		
	}

	/**
	 * Setter for Oddjob. This will change the Oddjob being monitored.
	 *  
	 * @param oddjob The new Oddjob or null to just close.
	 * 
	 * @throws PropertyVetoException If the exsiting Oddjob can't be
	 * closed.
	 */
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
	
	/**
	 * Add to the file history that will be used in the file menu.
	 * 
	 * @param file The file. If null then ignored.
	 */
	void addFileHistory(File file) {
		if (file == null) {
			return;
		}
		fileHistory.addHistory(file);
	}

	void updateFileMenu() {
		
		fileMenu.removeAll();
		if (newExplorerAction != null) {
			fileMenu.add(new JMenuItem(newExplorerAction));
		}
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
			
			State state = oddjob.lastStateEvent().getState();
			if (state.isStoppable()) {
				String message = "Oddjob is not stopped. Current state is " + state; 
				JOptionPane.showMessageDialog(frame, message, "Oddjob Running!", JOptionPane.ERROR_MESSAGE);
				throw new PropertyVetoException(message, evt);
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

		public void sessionModified(ConfigSessionEvent event) {
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
				// Because we DO_NOTHING_ON_CLOSE it's up to us to 
				// close the window.
				maybeCloseWindow();
			}

			public void windowClosed(WindowEvent e) {
				logger.debug("Explorer window closed.");
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
		final Oddjob oddjob = this.oddjob;
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

		frame.setVisible(true);
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				screen = new ScreenPresence(e.getComponent());
			}
			@Override
			public void componentResized(ComponentEvent e) {
				screen = new ScreenPresence(e.getComponent());
			}
		});
		
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					if(oddjob != null) {
						setOddjob(oddjob);
					} else if(getFile() != null) {
						open(getFile());
					}
				} catch (PropertyVetoException e) {
					// Ignore
				}
			}
		});
		
		while (!stop) {
			try {
				if (propertyPolling == null) {
					logger().info("No property polling. set ArooaSession to enable polling.");
				}
				else {
					propertyPolling.poll();
				}
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
		
		threadManager.close();
		
		return 0;	
	}
	
	/**
	 * Maybe close the window if it isn't vetoed be attempting to remove
	 * Oddjob.
	 */
	private void maybeCloseWindow() {

		try {
			setOddjob(null);
		} catch (PropertyVetoException e) {
			logger.info("Can't close: " + e.getMessage());
			return;
		}
		
		closeWindow();
	}
	
	/**
	 * Close window regardless. No veto here.
	 */
	private void closeWindow() {
		
		stop = true;
		
		// Wake up the log poller so it sees that we've stopped.
		synchronized (OddjobExplorer.this) {
			OddjobExplorer.this.notifyAll();
		}
		
		final JFrame frame = this.frame;
		if (frame != null) {
			// This hangs from the shutdown hook. I don't know
			// why. Invoking on the Event Dispatch thread makes no difference.
			if (!(Thread.currentThread() instanceof OddjobShutdownThread)) {
				frame.dispose();
			}
			this.frame = null;
		}
		
		logger().debug("Monitor closed.");
	}

	public void onStop() throws FailedToStopException {
		
		// Note that we close window first otherwise changing
		// window title deadlocks in the shutdown hook.
		closeWindow();
		
		Oddjob oddjob = this.oddjob;
		if (oddjob != null) {
			oddjob.stop();
			try {
				setOddjob(null);
			} catch (PropertyVetoException e) {
				// stop should mean this doesn't happen - but just in case:
				this.oddjob = null;
			}
		}
	}
	
	/**
	 * Helper method to create a new Oddjob.
	 * 
	 * @return A new Oddjob.
	 */
	private Oddjob newOddjob() {

		Oddjob oddjob = new Oddjob();
		oddjob.setArooaSession(getArooaSession());
		oddjob.setOddjobServices(oddjobServices);
		oddjob.setInputHandler(new SwingInputHandler(frame));
		return oddjob;
	}
	
	class NewExplorerAction extends AbstractAction {
		
		private static final long serialVersionUID = 2011090600;
		
		private final MultiViewController multiViewController;
		
		NewExplorerAction(MultiViewController multiViewController) {
			this.multiViewController = multiViewController;
			
			putValue(Action.NAME, "New Explorer");
			putValue(Action.MNEMONIC_KEY, Standards.NEW_EXPLORER_MNEMONIC_KEY); 
			putValue(Action.ACCELERATOR_KEY, Standards.NEW_EXPLORER_ACCELERATOR_KEY);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			multiViewController.launchNewExplorer(OddjobExplorer.this);
		}
	}
	
	
	class NewAction extends AbstractAction {
		private static final long serialVersionUID = 2008120400;
		
		NewAction() {
			putValue(Action.NAME, "New Oddjob");
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
			
			open(chooser.getSelectedFile());
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

	public int getFileHistorySize() {
		return fileHistory.size();
	}

    /**
     * @oddjob.property
     * @oddjob.description How many lines to keep in file history.
     * @oddjob.required No.
     */
	public void setFileHistorySize(int fileHistorySize) {
		this.fileHistory.setListSize(fileHistorySize);
	}

	public ScreenPresence getScreen() {
		return screen;
	}

	public String getLogFormat() {
		return logFormat;
	}

	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}
	
	private void open(File file) {
		try {
			Oddjob newJob = newOddjob();
			
			newJob.setFile(file);
			
			newJob.load();
			
			setOddjob(newJob);
		}
		catch (PropertyVetoException e1) {
			logger.info("Why?");			
		}
		catch (RuntimeException ex) {
			logger().warn("Exception opening file [" + file + "]", ex);
			JOptionPane.showMessageDialog(frame, ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public File getFile() {
		return file;
	}
	
	@ArooaAttribute
	public void setFile(File file) {
		this.file = file;
	}

}
