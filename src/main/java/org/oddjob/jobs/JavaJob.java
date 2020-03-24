package org.oddjob.jobs;


import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @oddjob.description Execute a Java Program in a separate process.
 *
 * @oddjob.example
 *
 * A simple example.
 *
 * {@oddjob.xml.resource org/oddjob/jobs/JavaExample.xml}
 *
 * @author Rob Gordon.
 */
public class JavaJob extends ExecBase {

	private static final long serialVersionUID = 2020032300L;

	/**
	 * @oddjob.property
	 * @oddjob.description The class name
	 * @oddjob.required yes.
	 */
	private String className;

	/**
	 * @oddjob.property
	 * @oddjob.description Space separated program arguments
	 * @oddjob.required no.
	 */
	private String programArgs;

	/**
	 * @oddjob.property
	 * @oddjob.description Space separated vm arguments
	 * @oddjob.required no.
	 */
	private String vmArgs;

	/**
	 * @oddjob.property
	 * @oddjob.description The class path of the java program.
	 * @oddjob.required no.
	 */
	private File[] classPath;


	@Override
	protected String[] provideArgs() throws Exception {

		String className = Objects.requireNonNull(this.className);

		List<String> args = new ArrayList<>();

		// java exe

		String javaHome = Objects.requireNonNull(
				System.getProperty("java.home"), "No java.home");

		List<File> maybeJava = Arrays.asList(
				new File(javaHome, "bin/java.exe"),
				new File(javaHome, "bin/java"));

		File javaPath = maybeJava.stream()
				.filter(File::exists)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Can't find java in " + maybeJava));

		String java = javaPath.getCanonicalPath();
		args.add(java);

		// classpath

		Optional.ofNullable(this.classPath)
				.map(files -> Arrays.stream(files)
						.map(File::toString)
						.collect(Collectors.joining(File.pathSeparator)))
				.ifPresent(path -> {
					args.add("-cp");
					args.add(path);
				});

		// vm args

		Optional.ofNullable(this.vmArgs)
				.map(a -> {
					try {
						return commandTokenizer().parse(a);
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				})
				.ifPresent(a -> args.addAll(Arrays.asList(a)));

		// class name

		args.add(className);

		// program args

		Optional.ofNullable(this.programArgs)
				.map(a -> {
					try {
						return commandTokenizer().parse(a);
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				})
				.ifPresent(a -> args.addAll(Arrays.asList(a)));

		return args.toArray(new String[0]);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getProgramArgs() {
		return programArgs;
	}

	public void setProgramArgs(String programArgs) {
		this.programArgs = programArgs;
	}

	public String getVmArgs() {
		return vmArgs;
	}

	public void setVmArgs(String vmArgs) {
		this.vmArgs = vmArgs;
	}

	public File[] getClassPath() {
		return classPath;
	}

	public void setClassPath(File[] classPath) {
		this.classPath = classPath;
	}
}
