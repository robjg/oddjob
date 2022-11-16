package org.oddjob.monitor;

import org.oddjob.OddjobServices;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.design.view.ScreenPresence;
import org.oddjob.framework.ExecutionWatcher;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.monitor.model.FileHistory;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateOperator;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @oddjob.description A container that allows multiple {@link OddjobExplorer}s to run.
 * This is the default job that Oddjob runs on startup.
 * 
 * @author rob
 *
 */
public class MultiExplorerLauncher extends StructuralJob<Runnable> 
implements Stoppable  {

	private static final long serialVersionUID = 2022111600L;
	
	@Override
	protected StateOperator getInitialStateOp() {
		return new AnyActiveStateOp();
	}
	
	/**
	 * @oddjob.property
	 * @oddjob.description Internal services. Set automatically
	 * by Oddjob.
	 * @oddjob.required No.
	 */
	private transient OddjobServices oddjobServices;

    /** 
     * @oddjob.property
     * @oddjob.description The directory the file chooser 
     * should use when opening and saving Oddjobs.
     * @oddjob.required No. 
     */
	private File dir;

    /** 
     * @oddjob.property
     * @oddjob.description A file to load when the explorer starts.
     * @oddjob.required No. 
     */
	private File file;

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
	
	private final FileHistory fileHistory;

	// These will be serialized so frame settings are preserved.
	private final AtomicReference<ScreenPresence> screen;
	
	/**
	 * Default constructor.
	 */
	public MultiExplorerLauncher() {

		this(new FileHistory(), ScreenPresence.defaultSize());
	}

	/**
	 * Used by custom deserialization.
	 */
	protected MultiExplorerLauncher(FileHistory fileHistory,
									ScreenPresence screen) {

		this.fileHistory = fileHistory;
		this.screen = new AtomicReference<>(screen);
	}

	@Inject
	public void setOddjobServices(OddjobServices oddjobServices) {
		this.oddjobServices = oddjobServices;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected void execute() throws InterruptedException {
		if (oddjobServices == null) {
			throw new NullPointerException("No Executor! Were services set?");
		}
		
		final ExecutionWatcher executionWatcher = 
				new ExecutionWatcher(() -> {
					stop = false;
					MultiExplorerLauncher.super.startChildStateReflector();
				});
		
		MultiViewController controller = new MultiViewController() {
			
			@Override
			public synchronized void launchNewExplorer(OddjobExplorer original) {
				
				if (original != null) {
					dir = original.getDir();
				}
				
				OddjobExplorer explorer = new OddjobExplorer(this, screen, fileHistory);
				explorer.setDir(MultiExplorerLauncher.this.dir);
				explorer.setPollingInterval(pollingInterval);
				explorer.setLogFormat(logFormat);
				explorer.setOddjobServices(oddjobServices);
				explorer.setArooaSession(getArooaSession());
				
				if (original == null) {
					// Only set the file for the first explorer.
					explorer.setFile(getFile());
				}
				
				childHelper.insertChild(childHelper.size(), explorer);
				
				oddjobServices.getOddjobExecutors().getPoolExecutor(
						).execute(executionWatcher.addJob(explorer));
			}
		};
		
		controller.launchNewExplorer(null);
		
		stateHandler().waitToWhen(new IsStoppable(), () -> getStateChanger().setState(ParentState.ACTIVE));
		
		executionWatcher.start();

	}

	@Override
	protected void startChildStateReflector() {
		// This is started by us so override and do nothing.
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		
		childHelper.removeAllChildren();
	}
	
	public File getDir() {
		return dir;
	}

	@ArooaAttribute
	public void setDir(File dir) {
		this.dir = dir;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}
	
	@ArooaAttribute
	public void setFile(File file) {
		this.file = file;
	}

	public long getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(long pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public int getFileHistorySize() {
		return fileHistory.getListSize();
	}

    /**
     * @oddjob.property fileHistorySize
     * @oddjob.description How many lines to keep in file history.
     * @oddjob.required No.
     */
	public void setFileHistorySize(int fileHistorySize) {
		this.fileHistory.setListSize(fileHistorySize);
	}

	public String getLogFormat() {
		return logFormat;
	}

	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}

	/**
	 * Custom de-serialisation. Ensure the resolved screen presence fits on any screen on
	 * the current device.
	 */
	private Object readResolve()
			throws IOException, ClassNotFoundException {

		if (screen.get().isOnAnyScreen()) {
			return this;
		}
		else {
			return new MultiExplorerLauncher(this.fileHistory, ScreenPresence.defaultSize());
		}
	}

}
