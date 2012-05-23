package org.oddjob.jobs;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.arooa.utils.ArooaTokenizer;
import org.oddjob.arooa.utils.QuoteTokenizerFactory;
import org.oddjob.framework.SerializableJob;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LoggingOutputStream;
import org.oddjob.logging.cache.LogArchiveImpl;
import org.oddjob.util.IO;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description Execute an external program. This job will
 * flag complete if the return state of the external program is 0,
 * otherwise it will flag not complete.
 * <p>
 * Processes may behave differently on different operating systems - for
 * instance stop doesn't always kill the process. Please see
 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do;:YfiG?bug_id=4109888">
 * http://bugs.sun.com/bugdatabase/view_bug.do;:YfiG?bug_id=4109888</a>
 * for additional information.
 * 
 * @oddjob.example
 * 
 * A simple example.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/ExecSimpleExample2.xml}
 * 
 * Oddjob will treat arguments in quotes as single program argument and allows
 * them to be escaped with backslash. If this is too confusing it is sometimes 
 * easier to specify the command as individual arguments. The
 * above is equivalent to:
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/ExecSimpleExample.xml}
 * 
 * @oddjob.example
 * 
 * Using the existing environment with an additional environment variable.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/ExecJobEnvironmentExample.xml}
 * 
 * @oddjob.example
 * 
 * Capturing console output to a file. The output is Oddjob's command
 * line help.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/ExecWithRedirectToFile.xml}
 * 
 * @oddjob.example
 * 
 * Capturing console output to the logger. Note how the logger output
 * can be defined with different log levels for stdout and sterr.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/ExecWithRedirectToLog.xml}
 * 
 * @oddjob.example
 *
 * Using the output of one process as the input of another. Standard input for 
 * the first process is provided by a buffer. A second buffer captures the
 * output of that process and passess it to the second process. The output
 * of the second process is captured and sent to the console of the parent
 * process.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/ExecWithStdInExample.xml}
 * 
 * @author Rob Gordon.
 */
public class ExecJob extends SerializableJob 
implements Stoppable, ConsoleOwner {
	private static final long serialVersionUID = 2009012700L;
	
	private static int consoleCount;
	
	private static String uniqueConsoleId() {
		synchronized (ExecJob.class) {
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
		this.environment = new HashMap<String, String>();
	}
	
    /**
     * @oddjob.property
     * @oddjob.description The working directory.
     * @oddjob.required No
     */
	private File dir;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The command to execute. The command is
	 * interpreted as space delimited text which may
	 * be specified over several lines. Arguments that need to
	 * include spaces must be quoted. Within quoted arguments quotes
	 * may be escaped using a backslash.
	 * @oddjob.required yes, unless args are
	 * provided instead.
	 */
	private String command;
	
	/**
	 * @oddjob.property
	 * @oddjob.description A string list of arguments.
	 * @oddjob.required No.
	 */
	private String[] args;
	
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
	 * The process.
	 */
	private transient volatile Process proc;
	
	private transient volatile Thread thread;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The exit value of the process.
	 */
	private int exitValue;
	
	public ExecJob() {
		completeConstruction();
	}
	
	/**
	 * Add an argument.
	 * 
	 * @param arg The argument.
	 */
	public void setArgs(String[] args) {
		this.args = args;	
	}

	/**
	 * Set the command to run.
	 * 
	 * @param command The command.
	 */
	@ArooaText
	public void setCommand(String command) {
		this.command = command;
	}
	
	/**
	 * Get the command.
	 * 
	 * @return The command.
	 */
	public String getCommand() {
		return command;
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
	 * @param nvp The name/value pair variable.
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
	
	/**
	 * Provide the {@link ArooaTokenizer} to use for parsing commands.
	 * 
	 * @return A tokenizer, never null.
	 */
	public ArooaTokenizer commandTokenizer() {
		return new QuoteTokenizerFactory(
				"\\s+", '"', '\\').newTokenizer();
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		
		ProcessBuilder processBuilder;
		
		String[] theArgs = args;
		if (theArgs == null && command != null) {
			
			theArgs = commandTokenizer().parse(command.trim());
			logger().info("Command: " + command);
		} 
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
			for (Map.Entry<String, String> entry: environment.entrySet()) {
				env.put(entry.getKey(), entry.getValue());
			}
		}

		processBuilder.redirectErrorStream(redirectStderr);
		
		proc = processBuilder.start();
		
		final InputStream processStdOut = proc.getInputStream();

		Thread outT = new Thread(new Runnable() {			
			public void run() {		
				try {
					BufferedInputStream bis = new BufferedInputStream(processStdOut);
					OutputStream os = new LoggingOutputStream(stdout, LogLevel.INFO, 
							consoleArchive);
					IO.copy(bis, os);
					os.close();
					bis.close();
				} catch (IOException e) {
					logger().error("Failed copying stream.", e);
				}
			}
		});
		outT.start();		
		
		Thread  errT = null;
		if (!redirectStderr) { 
			final InputStream processStdErr = proc.getErrorStream();
			errT = new Thread(new Runnable() {

				public void run() {
					try {
						BufferedInputStream bis = new BufferedInputStream(
								processStdErr);
						OutputStream os = new LoggingOutputStream(stderr, 
								LogLevel.ERROR, consoleArchive);
						IO.copy(bis, os);
						os.close();
						bis.close();
					} catch (IOException e) {
						logger().error("Failed copying stream.", e);
					}
				}	
			});
	
			errT.start();
		}
		
		// copy input.
		if (stdin != null) {
			OutputStream processStdIn = proc.getOutputStream();
			IO.copy(stdin, processStdIn);
			stdin.close();
			processStdIn.close();
		}

		thread = Thread.currentThread();
		try {
			logger().debug("Waiting for process.");
			exitValue = proc.waitFor();
			logger().debug("Process completed with exit value " + exitValue);
		}
		finally {
			thread = null;

			proc.destroy();
			proc = null;
			
			synchronized (this) {
				// wake up the stop wait.
				notifyAll();
			}
		}

		return exitValue;
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
		proc.destroy();
		
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
