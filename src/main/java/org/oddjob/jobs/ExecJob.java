package org.oddjob.jobs;


import org.oddjob.arooa.deploy.annotations.ArooaText;

/**
 * @oddjob.description Execute an external program. This job will
 * wait for the process to terminate and be
 * COMPLETE if the return state of the external program is 0,
 * otherwise it will be NOT COMPLETE.
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
public class ExecJob extends ExecBase {

	private static final long serialVersionUID = 2020032300L;


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
	

	/**
	 * Add an argument.
	 * 
	 * @param args The arguments.
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

	@Override
	protected String[] provideArgs() throws Exception {
		String[] theArgs = args;

		if (theArgs == null && command != null) {

			theArgs = commandTokenizer().parse(command.trim());
			logger().info("Command: " + command);
		}

		return theArgs;
	}

}
