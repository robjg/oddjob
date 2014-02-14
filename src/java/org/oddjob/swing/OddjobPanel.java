package org.oddjob.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import org.oddjob.FailedToStopException;
import org.oddjob.Iconic;
import org.oddjob.OddjobServices;
import org.oddjob.OddjobShutdownThread;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.Version;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.design.view.ScreenPresence;
import org.oddjob.arooa.registry.ServiceProvider;
import org.oddjob.arooa.registry.Services;
import org.oddjob.framework.SimpleService;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;
import org.oddjob.input.InputHandler;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * Provide a simple panel with buttons on, that run Oddjob jobs.
 * <p>
 * 
 * @author rob
 * 
 * @since 1.3
 * 
 */
public class OddjobPanel extends SimpleService
implements ServiceProvider, Services, Serializable, Stoppable, Structural {
	
	private static final long serialVersionUID = 2012091900L;

	protected transient ChildHelper<Object> childHelper; 
	
	/** The executor to use. */
	private volatile transient ExecutorService executorService;

	/** The job threads. */
	private volatile transient List<JobButtonAction> actions;
		
	// These will be serialised so frame settings are preserved.
	private ScreenPresence screen;
	
	private int columns = 2;
	
	/** The frame */
	private volatile transient FrameWithStatus frame;
	
	/**
	 * Constructor.
	 */
	public OddjobPanel() {
		completeConstruction();
		
		ScreenPresence whole = ScreenPresence.wholeScreen();
		screen = whole.smaller(0.33);		
	}
	
	private void completeConstruction() {
		childHelper = new ChildHelper<Object>(this);
	}
		
	/**
	 * Set the {@link ExecutorService}.
	 * 
	 * @oddjob.property executorService
	 * @oddjob.description The ExecutorService to use. This will 
	 * be automatically set by Oddjob.
	 * @oddjob.required No.
	 * 
	 * @param child A child
	 */
	@Inject
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	protected JComponent createPanel() {
		
		actions = new ArrayList<JobButtonAction>();
		
		Object[] jobs = childHelper.getChildren();
		
		int rows = (int) Math.ceil((double) jobs.length / columns);
		
		JPanel panel = new JPanel(new GridLayout(rows, columns, 10, 10));
				
		for (Object child : jobs) {
		
			if (! (child instanceof Runnable)) {
				logger().info("Child [" + child + 
						"] is not Runnable - ignoring.");
				continue;
			}
			
			final Runnable job = (Runnable) child;

			JobButtonAction action = new JobButtonAction(job);
			
			actions.add(action);
			
			JButton button = new JButton(action);
			
			panel.add(button);
			
			if (job instanceof Stateful) {
				((Stateful) job).addStateListener(new StateListener() {
					
					@Override
					public void jobStateChange(StateEvent event) {
						if (StateConditions.FINISHED.test(
								event.getState())) {
							
							String status = job.toString() + ": " +
									event.getState();
							if (event.getException() != null) {
								status += " " + event.getException();
							}
							
							frame.setStatus(status);
						}
					}
				});
			}
		}
		
		JPanel padding = new JPanel();
		padding.add(panel);
		
		return padding;
	}
	
	@Override
	protected void onStart() throws Throwable {
		if (executorService == null) {
			throw new NullPointerException("No Executor Service.");
		}

		JComponent panel = createPanel();
		
		
		JScrollPane scroll = new JScrollPane(panel);
		
		frame = new FrameWithStatus();
		screen.fit(frame);
		
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
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// Because we DO_NOTHING_ON_CLOSE it's up to us to 
				// close the window.
				try {
					stop();
				} catch (FailedToStopException e1) {
					logger().error(e);
				}
			}

			public void windowClosed(WindowEvent e) {
				logger().debug("Panel window closed.");
			}
		});
		
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setTitle(this.toString());
		
		scroll.setBorder(BorderFactory.createEtchedBorder(
				EtchedBorder.RAISED));
		frame.getContentPane().add(scroll, BorderLayout.CENTER);
		
		frame.setVisible(true);
		
		logger().debug("Panel started.");
	}

	@Override
	protected void onStop() throws FailedToStopException {
		
		final JFrame frame = this.frame;
		if (frame != null) {
			if (!(Thread.currentThread() instanceof OddjobShutdownThread)) {
				frame.dispose();
			}
			this.frame = null;
		}
		
		for (JobButtonAction action : actions) {
			action.externalStop();
		}
		
		logger().debug("Panel closed.");
	}
	
	/**
	 * Add a listener. The listener will immediately receive add
	 * notifications for all existing children.
	 * 
	 * @param listener The listener.
	 */	
	public void addStructuralListener(StructuralListener listener) {
		stateHandler().assertAlive();
		
		childHelper.addStructuralListener(listener);
	}
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}	
			
	
	/**
	 * Add a child.
	 * 
	 * @oddjob.property jobs
	 * @oddjob.description The child jobs.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param child A child
	 */
	@ArooaComponent
	public void setJobs(int index, Runnable child) {
		if (child == null) {
			childHelper.removeChildAt(index);
		}
		else {
			childHelper.insertChild(index, child);
		}
	}		
	
	@Override
	public Services getServices() {
		return this;
	}

	@Override
	public Object getService(String serviceName)
			throws IllegalArgumentException {
		if (OddjobServices.INPUT_HANDLER.equals(serviceName)) {
			if (frame == null) {
				return null;
			}
			else {
				return new SwingInputHandler(frame);
			}
		}
		else {
			throw new IllegalArgumentException("No service "  + serviceName);
		}
	}
	
	@Override
	public String serviceNameFor(Class<?> theClass, String flavour) {
		if (theClass.isAssignableFrom(InputHandler.class)) {
			return OddjobServices.INPUT_HANDLER;
		}
		else {
			return null;
		}
	}
	
	class JobButtonAction extends AbstractAction {
		private final static long serialVersionUID = 2012091900L;
		
		private final Runnable job;
		
		private volatile Future<?> future;
		
		private volatile Runnable clickTask;
		
		private volatile Runnable stopTask;
		
		JobButtonAction(Runnable job) {
			super(job.toString());
			this.job = job;
			
			if (job instanceof Iconic) {
				
				((Iconic) job).addIconListener(new IconListener() {
					
					@Override
					public void iconEvent(IconEvent e) {
						putValue(SMALL_ICON, 
								e.getSource().iconForId(e.getIconId()));
					}
				});
			}			
			
			resetActions();
		}
		
		void resetActions() {
			
			clickTask = new RunAction();
			stopTask = new NoopAction();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
						
			clickTask.run();
		}
		
		void externalStop() {
			
			stopTask.run();
		}
		
		class RunAction implements Runnable {
						
			@Override
			public void run() {
				
				future = executorService.submit(
				new Runnable() {
					
					@Override
					public void run() {
						if (job instanceof Resetable) {
							((Resetable) job).hardReset();
						}
						
						job.run();
						
						synchronized (RunAction.this) {
							resetActions();
						}
					}
				});
				
				synchronized (this) {
					if (clickTask == this) {
						clickTask = new Stop();
						stopTask = clickTask;
					}
				}
			}
		}
		
		class NoopAction implements Runnable {
			
			@Override
			public void run() {
				// No operation.
			}
		}
		
		
		class Stop implements Runnable {
			
			@Override
			public void run() {
				
				if (future != null) {
					future.cancel(false);
				}
				
				if (job instanceof Stoppable) {
					
					try {
						((Stoppable) job).stop();
					} catch (FailedToStopException e) {
						logger().error(e);
					}
				}
			}
		}
	}

	
	
	public int getColumns() {
		return columns;
	}

	public void setColumns(int cols) {
		this.columns = cols;
	}
	
	public ScreenPresence getScreen() {
		return screen;
	}
	
	/**
	 * Custom serialisation.
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

	
	static class FrameWithStatus extends JFrame {
		private static final long serialVersionUID = 2012092800L;
		
		private final JLabel status = new JLabel(Version.getCurrentFullBuildMessage());
		
		public FrameWithStatus() {
			Container container = getContentPane();
			container.setLayout(new BorderLayout());
			
			container.add(status, BorderLayout.SOUTH);
		}		
		
		public void setStatus(String status) {
			this.status.setText(status);
		}
	}
}
