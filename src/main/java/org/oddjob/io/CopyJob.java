package org.oddjob.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.beanbus.Destination;
import org.oddjob.util.OddjobConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;


/**
 * @oddjob.description A Copy job. Copy either:
 * <ul>
 *   <li>A file from one name to another.</li> 
 *   <li>A file or files or directories to a different directory.</li>
 *   <li>An input (from another job) to a file.</li>
 *   <li>A file to an output.</li>
 *   <li>An input to an output.</li>
 *   <li>A file or input by lines to a Consumer such as a Bean Bus Destination</li>
 * </ul>
 * 
 * @oddjob.example
 * 
 * Copy a file.
 * 
 * {@oddjob.xml.resource org/oddjob/io/CopyFileExample.xml}
 * 
 * @oddjob.example
 * 
 * Copy a directory.
 * 
 * {@oddjob.xml.resource org/oddjob/io/CopyDirectory.xml}
 * 
 * @oddjob.example
 * 
 * Copy from a file to a buffer.
 * 
 * {@oddjob.xml.resource org/oddjob/io/CopyFileToBuffer.xml}
 *
 * @oddjob.example
 *
 * Copy into a Bean Bus. The lines from the file are copied into
 * the bus where they are mapped with a function that appends 'Foo' before writing
 * them out in another file.
 *
 * {@oddjob.xml.resource org/oddjob/io/CopyFileByLines.xml}
 *
 */
public class CopyJob implements Runnable, Serializable {
    private static final long serialVersionUID = 20050806;

	private static final Logger logger = LoggerFactory.getLogger(CopyJob.class);
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The file to read from.
	 * @oddjob.required Yes unless input supplied.
	 */
	private File[] from;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The file to write to.
	 * @oddjob.required Yes unless output supplied.
	 */
	private File to;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description An input stream.
	 * @oddjob.required Yes unless from supplied.
	 */
	private transient InputStream input;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description An output stream.
	 * @oddjob.required Yes unless to is supplied.
	 */
	private transient OutputStream output;

	/**
	 * @oddjob.property
	 * @oddjob.description A consumer of strings. Intended for use as the driver in a
	 * {@link org.oddjob.beanbus.bus.BasicBusService}.
	 * @oddjob.required No. Will be set automatically in a Bean Bus.
	 */
	private transient Consumer<? super String> consumer;

	/**
	 * @oddjob.property
	 * @oddjob.description The number of files copied.
	 * @oddjob.required Read Only.
	 */
	private int filesCopied;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The number of directories copied.
	 * @oddjob.required Read Only.
	 */
	private int directoriesCopied;
	
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the from file.
	 * 
	 * @return The from file.
	 */
	synchronized public File[] getFrom() {
		return from;
	}
	
	/**
	 * Set the From file.
	 * 
	 * @param file The from file.
	 */
	synchronized public void setFrom(File[] file) {
		this.from = file;
	}

	/**
	 * Get the to file.
	 * 
	 * @return The to file.
	 */
	synchronized public File getTo() {
		return to;
	}
	
	/**
	 * Set the to file.
	 * 
	 * @param file The to file.
	 */
	@ArooaAttribute
	synchronized public void setTo(File file) {
		this.to = file;
	}

	/**
	 * Set the InputStream.
	 * 
	 * @param in The InputStream.
	 */
	synchronized public void setInput(InputStream in) {
		this.input = in;
	}

	/**
	 * Set the OutputStream.
	 * 
	 * @param out The OutputStream.
	 */
	synchronized public void setOutput(OutputStream out) {
		this.output = out;
	}

	public int getFilesCopied() {
		return filesCopied;
	}
	
	public int getDirectoriesCopied() {
		return directoriesCopied;
	}
	
	public void run() {
		try (CopyCommand command = command()) {

			logger.info("Performing {}", command);
			
			CopyStats stats = new CopyStats();
			command.copy(stats);
			logger.info("Copied {} files, {} directories.", stats.files, stats.directories );
			this.filesCopied = stats.files;
			this.directoriesCopied = stats.directories;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
		
	private CopyCommand command() throws IOException {


		if (input != null) {
			if (consumer != null) {
				return new ConsumerCopy(input, consumer);
			}
			if (output != null) {
				return new StreamCopy(input, output); 
			}
			else {
				return new StreamCopy(input, to);
			}
		}
	    
		if (from == null) {
	    	throw new RuntimeException("Nothing to copy from!");
	    }

		File[] possiblyMany = FilesUtil.expand(from);
	    FilesUtil.verifyReadable(possiblyMany);
		File singleFrom = null;
		
	    if (possiblyMany.length == 0) {
	    	throw new RuntimeException("From does not specify any files.");
	    }
	    
	    if (possiblyMany.length == 1) {
	    	singleFrom = possiblyMany[0];
	    }

		if (singleFrom != null) {
			if (consumer != null) {
				return new ConsumerCopy(singleFrom, consumer);
			}
			else if (output != null) {
				return new StreamCopy(singleFrom, output);
			}
			else if (singleFrom.isDirectory()) {
				return new DirectoryCopy(singleFrom, to);
			} else {
				return new FileCopy(singleFrom, to);
			}
		}
		
		return new MultiFileCopy(possiblyMany, to);
	}
	
	/**
	 * @return Returns the input.
	 */
	public InputStream getInput() {
		return input;
	}
	/**
	 * @return Returns the output.
	 */
	public OutputStream getOutput() {
		return output;
	}
	
	public String toString() {
		if (name == null) {
			return "Copy Files or Directories";
		}
		return name;
	}

	@Destination
	public void setConsumer(Consumer<? super String> consumer) {
		this.consumer = consumer;
	}

	public Consumer<? super String> getConsumer() {
		return consumer;
	}

	/**
	 * 
	 *
	 */
	interface CopyCommand extends Closeable {
	
		void copy(CopyStats stats) throws IOException;
	}
	
	static class StreamCopy implements CopyCommand {
		private final InputStream in;
		private final OutputStream out;
		
		StreamCopy(InputStream in, OutputStream out) {
			this.in = in;
			this.out = out;
		}
		
		StreamCopy(InputStream in, File to) throws IOException {
			this.in = in;
	    	if (to == null) {
				throw new NullPointerException("Nothing to copy the input to.");
	    	}
	    	if (to.isDirectory()) {
	    		throw new OddjobConfigException("Can't copy stream to a directory.");
	    	}
	    	this.out = Files.newOutputStream(to.toPath());
		}
		
		StreamCopy(File from, OutputStream out) throws IOException {
			this.in = Files.newInputStream(from.toPath());
			this.out = out; 
		}
		
		@Override
		public void copy(CopyStats stats) throws IOException {
			IOUtils.copy(in, out);

			stats.files++;
		}

		@Override
		public void close() throws IOException {
			try {
				in.close();
			} catch (Exception e) {
				// ignore.
			}
			out.close();
		}

		@Override
		public String toString() {
			return "Copy between to binary streams.";
		}
	}
	
	static class FileCopy implements CopyCommand {
		private final File from;
		private final File to;
		
		FileCopy(File from, File to) {
			this.from = from;
			this.to = Objects.requireNonNull(to, "Nothing to copy the file to.");
		}
		
		@Override
		public void copy(CopyStats stats) throws IOException {
			if (to.isDirectory()) {
				FileUtils.copyFileToDirectory(from, to);
			} 
			else {
				FileUtils.copyFile(from, to);
			}
			stats.files++;
		}

		@Override
		public void close() throws IOException {
			// Close performed by file copy.
		}

		@Override
		public String toString() {
			return "File copy from " + from + " to " + to;
		}
	}
	
	
	static class DirectoryCopy implements CopyCommand {
		private final File fromDir;
		private final File toDir;
		
		DirectoryCopy(File from, File to) {
			this.fromDir = from;
	    	if (to == null) {
	    		throw new RuntimeException("To dir not specified.");
	    	}
	    	if (to.exists()) {
	    		if (!to.isDirectory()) {
	    			throw new OddjobConfigException("To must be a directory.");	    		
		    	}
	    		this.toDir = new File(to, from.getName());
	    	} else {
	    		this.toDir = to;
	    	}
		}
		
		@Override
		public void copy(CopyStats stats) throws IOException {
			FileUtils.copyDirectory(fromDir, toDir);
			stats.directories++;
		}

		@Override
		public void close() throws IOException {
			// Close performed by file copy.
		}

		@Override
		public String toString() {
			return "Directory copy from " + fromDir + " to " +
				toDir;
		}
	}

	static class MultiFileCopy implements CopyCommand {
		private final File[] files;
		private final File toDir;
		
		MultiFileCopy(File[] files, File toDir) {
			this.files = files;
	    	if (toDir == null) {
	    		throw new RuntimeException("To dir is not specified.");
	    	}
	    	if (!toDir.isDirectory()) {
	    		throw new RuntimeException("To must be a directory.");	    		
	    	}
			this.toDir = toDir;
		}
		
		@Override
		public void copy(CopyStats stats) throws IOException {
			for (File file : files) {
				try (CopyCommand command = file.isDirectory()
						? new DirectoryCopy(file, toDir) : new FileCopy(file, toDir)) {
					command.copy(stats);
				}
			}
		}

		@Override
		public void close() throws IOException {
			// Close performed by file copy.
		}

		@Override
		public String toString() {
			return "Multiple file copy of " + files.length +
					" files to " + toDir;
		}
	}

	static class ConsumerCopy implements CopyCommand {

		private final InputStream in;

		private final Consumer<? super String> out;

		ConsumerCopy(InputStream in, Consumer<? super String> out) {
			this.in = in;
			this.out = out;
		}

		ConsumerCopy(File from, Consumer<? super String> out) throws IOException {
			this.in = Files.newInputStream(from.toPath());
			this.out = out;
		}

		@Override
		public void copy(CopyStats stats) throws IOException {

			new BufferedReader(
					new InputStreamReader(in, StandardCharsets.UTF_8))
					.lines()
					.forEach(out);

			stats.files++;
		}

		@Override
		public void close() throws IOException {
			in.close();
		}

		@Override
		public String toString() {
			return "Copy from " + in + " to Consumer " + out;
		}
	}

	static class CopyStats {
		int files;
		int directories;
	}
}
