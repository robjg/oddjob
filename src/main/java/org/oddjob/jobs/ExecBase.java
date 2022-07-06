package org.oddjob.jobs;


import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.types.MapType;
import org.oddjob.arooa.utils.ArooaTokenizer;
import org.oddjob.arooa.utils.QuoteTokenizerFactory;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LoggingOutputStream;
import org.oddjob.logging.cache.LogArchiveImpl;
import org.oddjob.util.IO;
import org.oddjob.util.OddjobConfigException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for Jobs that create an external process.
 *
 * @author Rob Gordon.
 */
abstract public class ExecBase extends SerializableJob
implements Stoppable, ConsoleOwner {

	private static final long serialVersionUID = 2020032300L;

	private static int consoleCount;

	private static String uniqueConsoleId() {
		synchronized (ExecBase.class) {
			return ("EXEC_CONSOLE" + consoleCount++);
		}
	}

	private transient LogArchiveImpl consoleArchive;

	/**
	 * Complete construction.
	 */
	private void completeConstruction() {
		consoleArchive = new LogArchiveImpl(
				uniqueConsoleId(), LogArchiver.MAX_HISTORY);
		this.environment = new HashMap<>();
	}

    /**
     * @oddjob.property
     * @oddjob.description The working directory.
     * @oddjob.required No
     */
	private File dir;


	private boolean newEnvironment;

	/**
	 * @oddjob.property environment
	 * @oddjob.description An environment variable to be
	 * set before the program is executed. This is a
	 * {@link MapType} like property.
	 * @oddjob.required No.
	 */
	private Map<String, String> environment;

	private boolean redirectStderr;

	/**
	 * @oddjob.property
	 * @oddjob.description An input stream which will
	 * act as stdin for the process.
	 * @oddjob.required No.
	 */
	private transient InputStream stdin;

	/**
	 * @oddjob.property
	 * @oddjob.description An output to where stdout
	 * for the process will be written.
	 * @oddjob.required No.
	 */
	private transient OutputStream stdout;

	/**
	 * @oddjob.property
	 * @oddjob.description An output to where stderr
	 * of the proces will be written.
	 * @oddjob.required No.
	 */
	private transient OutputStream stderr;

	/**
	 * @oddjob.property
	 * @oddjob.description Forcibly stop the process on stop.
	 * @oddjob.required No, defaults to false.
	 */
	private transient boolean stopForcibly;

	/**
	 * The process.
	 */
	private transient volatile Process proc;

	private transient volatile Thread thread;

	/**
	 * @oddjob.property
	 * @oddjob.description The exit value of the process.
	 */
	private int exitValue;

	public ExecBase() {
		completeConstruction();
	}
	
	/**
	 * Set the working directory.
	 * 
	 * @param dir The working directory.
	 */
	@ArooaAttribute
	public void setDir(File dir) {
		this.dir = dir;
	}

	/**
	 * @oddjob.property newEnvironment
	 * @oddjob.description Create a fresh/clean environment. 
	 * @oddjob.required No.
	 */
	public void setNewEnvironment(boolean explicitEnvironment) {
		this.newEnvironment = explicitEnvironment;
	}
	
	public boolean isNewEnvironment() {
		return newEnvironment;
	}
	
	/**
	 * Add an environment variable.
	 *
	 * @param name The of the environment variable.
	 * @param value The value of the environment variable.
	 */
	public void setEnvironment(String name, String value) {
		if (value == null) {
			this.environment.remove(name);
		}
		else {
			this.environment.put(name, value);			
		}
	}

	public String getEnvironment(String name) {
		return this.environment.get(name);
	}
	
	/**
	 * @oddjob.property redirectStderr
	 * @oddjob.description Redirect the standard error stream in
	 * standard output.
	 * @oddjob.required No.
	 */
	public void setRedirectStderr(boolean redirectErrorStream) {
		this.redirectStderr = redirectErrorStream;
	}
	
	public boolean isRedirectStderr() {
		return this.redirectStderr;
	}
	
	/**
	 * Set the input stream stdin for the process will
	 * be read from.
	 * 
	 * @param stdin An InputStream.
	 */
	public void setStdin(InputStream stdin) {
	    this.stdin = stdin;
	}

	/**
	 * Get the input stream for stdin. This will be null unless one has
	 * been provided.
	 * 
	 * @return An InputStream or null.
	 */
	public InputStream getStdin() {
		return stdin;
	}
	
	/**
	 * Set the output stream stdout from the process will
	 * be directed to.
	 * 
	 * @param stdout The output stream.
	 */
	public void setStdout(OutputStream stdout) {
	    this.stdout = stdout;
	}

	/**
	 * Get the output stream for stdout. This will be null unless one has
	 * been provided.
	 * 
	 * @return An OutputStream or null.
	 */
	public OutputStream getStdout() {
		return stdout;
	}
	
	/**
	 * Set the output stream stderr from the process will
	 * be directed to.
	 * 
	 * @param stderr The error stream.
	 */
	public void setStderr(OutputStream stderr) {
	    this.stderr = stderr;
	}
	
	/**
	 * Get the output stream for stderr. This will be null unless one has
	 * been provided.
	 * 
	 * @return An OutputStream or null.
	 */
	public OutputStream getStderr() {
		return stderr;
	}

	public boolean isStopForcibly() {
		return stopForcibly;
	}

	public void setStopForcibly(boolean stopForcibly) {
		this.stopForcibly = stopForcibly;
	}

	/**
	 * Provide the {@link ArooaTokenizer} to use for parsing commands.
	 *
	 * @return A tokenizer, never null.
	 */
	public ArooaTokenizer commandTokenizer() {
		return new QuoteTokenizerFactory(
				"\\s+", '"', '\\').newTokenizer();

	}

	abstract protected String[] provideArgs() throws Exception;

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		
		ProcessBuilder processBuilder;

		String[] theArgs = provideArgs();
		if (theArgs == null || theArgs.length == 0) {
				throw new OddjobConfigException("No command given.");
		}
		
		logger().info("Args: " + displayArgs(theArgs));
		
		processBuilder = new ProcessBuilder(theArgs);
		
		if (dir == null) {
			dir = processBuilder.directory();
		}
		else {
			processBuilder.directory(dir);
		}

		Map<String, String> env = processBuilder.environment();
		
		if (newEnvironment) {
			env.clear();
		}
		
		if (environment != null) {
			env.putAll(environment);
		}

		processBuilder.redirectErrorStream(redirectStderr);
		
		proc = processBuilder.start();
		
		Thread outThread = 
				new CopyStream("stdout", proc.getInputStream(), stdout);
		outThread.start();		
		
		Thread errThread = null;
		if (!redirectStderr) { 
			errThread = new CopyStream("stderr", 
					proc.getErrorStream(), stderr);
			errThread.start();
		}
		
		OutputStream processStdIn = proc.getOutputStream();
		// copy input.
		if (stdin != null) {
			IO.copy(stdin, processStdIn);
			stdin.close();
		}
		processStdIn.close();

		thread = Thread.currentThread();
		try {
			logger().debug("Waiting for process.");
			exitValue = proc.waitFor();
			logger().info("Process completed with exit value " + exitValue);
		}
		finally {
			thread = null;

			// On linux this hangs sometime for reasons unknown.
			if (errThread != null) {
				errThread.join(3000L);
			}
			outThread.join(3000L);
			
			// Destroy is required even if the process has terminated
			// because otherwise file descriptors are left open.
			// This must happen after the stream readers have finished
			// otherwise it causes Stream Closed exceptions.
			proc.destroy();
			
			synchronized (this) {
				// wake up the stop wait.
				notifyAll();
			}
		}

		return exitValue;
	}

	private class CopyStream extends Thread {
		
		private final String name;
		
		private final InputStream stream;
		
		private final OutputStream to;
		
		public CopyStream(String name, InputStream stream, OutputStream to) {
			this.name = name;
			this.stream = stream;
			this.to = to;
		}
		
		public void run() {
			OutputStream os = new LoggingOutputStream(to, 
					LogLevel.ERROR, consoleArchive);
			try {
				IO.copy(stream, os);
			} catch (IOException e) {
				// Check process hasn't been destroyed. If it has then
				// there could be an intermittent java.io.IOException: Bad file descriptor
				// which might be related to this issue:
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5101298.
				if (!stop) {
					logger().error("Failed copying process " + name + ".", e);
				}
			}
			finally {
				try {
					os.close();
				} catch (IOException e) {
					logger().error("Failed closing os for " + name + ".", e);
				}
				try {
					stream.close();
				} catch (IOException e) {
					logger().error("Failed closing process os for " + name + ".", e);
				}
				
			}
		}	
	}
	
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.framework.BaseComponent#onStop()
	 */
	public void onStop() {
		
		Process proc = this.proc;
		if (proc == null) {
			return;
		}

		if (stopForcibly) {
			proc.destroyForcibly();
		}
		else {
			proc.destroy();
		}

		for (int i = 0; i < 3 && thread != null; ++i) {
			synchronized (this) {
				try {
					logger().debug("Waiting for process to die.");
					wait(1000);
				}
				catch (InterruptedException e) {
					return;
				}
			}
		}
		
		Thread thread = this.thread;
		if (thread != null) {
			logger().warn("Process failed to die - needs to be manually killed.");
			thread.interrupt();
		}
	}	

	/**
	 * @return Returns the dir.
	 */
	public File getDir() {
		return dir;
	}
	
	
	public int getExitValue() {
		return exitValue;
	}

	public LogArchive consoleLog() {
		return consoleArchive;
	}
	
	/*
	 * Custome serialization.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}
	
	/*
	 * Custome serialization.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}

	private static String displayArgs(String[] args) {
		StringBuilder builder = new StringBuilder();
			for (String arg: args) {
				if (builder.length() > 0) {
					builder.append(' ');
				}
				builder.append('[');
				builder.append(arg);
				builder.append(']');
			}
		return builder.toString();
	}
}
