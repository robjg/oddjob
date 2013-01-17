package org.oddjob.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @oddjob.description Search files or an input stream for lines containing
 * a text value or matches for a regular expression.
 * 
 * @oddjob.example
 * 
 * Search a buffer of text for the word red. In this example the
 * search is case insensitive and the results a written to the console
 * with the line number. 
 * 
 * {@oddjob.xml.resource org/oddjob/io/GrepJobExample.xml}
 * 
 * 
 * 
 * @author rob
 *
 */
public class GrepJob implements Callable<Integer> {

	/** 
	 * @oddjob.property
	 * @oddjob.description A display name for the job.
	 * @oddjob.required No. 
	 */
	private String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The files to search.
	 * @oddjob.required No, not if an in is provided.
	 */
	private File[] files;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The input to search.
	 * @oddjob.required No, not if files are provided.
	 */
	private InputStream in;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Text to search for.
	 * @oddjob.required No, not if a regexp is provided. 
	 */
	private String text;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A regular expression to match.
	 * @oddjob.required No, not if text is provided. 
	 */
	private String regexp;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Where to write output to.
	 * @oddjob.required No. If not provided no output will be written 
	 */
	private OutputStream out;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A count of the number of matched lines.
	 * @oddjob.required Read Only.
	 */
	private int matchedLineCount;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A collection for {@link GrepLineResult} beans
	 * to be written to.
	 * @oddjob.required No.
	 */
	private Collection<? super GrepLineResult> results;

	/** 
	 * @oddjob.property
	 * @oddjob.description Prefix output with line numbers. If true
	 * then the number of the match in the file or input will be prepended 
	 * to each line of output.
	 * @oddjob.required No. Default to false.
	 */
	private boolean lineNumbers;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Remove the path from the file name. If true
	 * and a file name is prefixed to each line of output, then the path
	 * is removed.
	 * @oddjob.required No. Default to false.
	 */
	private boolean noPath;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Don't prefix output with a file name. If true
	 * then no file name will be prefixed to each line of output.
	 * @oddjob.required No. Default to false.
	 */
	private boolean noFilename;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Prefix output with a file name. If true
	 * then the file name will be prefixed to each line of output. By
	 * default the file name is not prefixed to a single file, only when
	 * there are multiple files being searched. This property will prefix
	 * the file name when only a single file is being searched.
	 * @oddjob.required No. Default to false.
	 */
	private boolean withFilename;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Ignore case. If true, the search will be case
	 * insensitive.
	 * @oddjob.required No. Default to false.
	 */
	private boolean ignoreCase;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Invert the search. If true, then only lines that
	 * don't contain a match will be output.
	 * @oddjob.required No. Default to false.
	 */
	private boolean invert;
	
	public Integer call() throws IOException {
		
		matchedLineCount = 0;
		
		int flags = 0;
		if (ignoreCase) {
			flags = Pattern.CASE_INSENSITIVE;
		}
		
		Pattern grep;
		if (text != null) {
			grep = Pattern.compile(Pattern.quote(text), flags);
		}
		else if (regexp != null) {
			grep = Pattern.compile(regexp, flags);
		}
		else {
			throw new NullPointerException("Nothing to search for.");
		}
		
		PrintStream resultStream;
		if (out == null) {
			resultStream = null;
		}
		else {
			resultStream = new PrintStream(out);
		}
		
		GrepHandler grepHandler;
		if (files != null) {
			grepHandler = new FilesGrepHandler(resultStream);
		}
		else if (in != null){
			grepHandler = new StreamGrepHandler(resultStream);
		}
		else {
			throw new NullPointerException("No Input.");
		}
		
		try {
			while (true) {
				LineNumberReader reader = grepHandler.nextReader();
				if (reader == null) {
					break;
				}
				
				try {
					while (true) {
						String line = reader.readLine();
						if (line == null) {
							break;
						}
						
						Matcher matcher = grep.matcher(line);
		
						if (matcher.find() ^ invert) {
							
							matchedLineCount++;
							int lineNumber = reader.getLineNumber();
							
							String match = null;
							if (!invert) {
								match = matcher.group();
							}
							
							grepHandler.processResult(lineNumber, 
									line, match);
						}
					}
				}
				finally {
					reader.close();
				}
			}
		}
		finally {
			if (resultStream != null) {
				resultStream.close();
			}
		}
		
		return 0;
	}
	
	/**
	 * Allow for different handling when reading files and streams.
	 *
	 */
	private interface GrepHandler {
		
		LineNumberReader nextReader() throws FileNotFoundException;
		
		void processResult(int lineNumber, String line, String match);
	}
	
	private class FilesGrepHandler implements GrepHandler {
		
		private final PrintStream resultStream;
		
		private int fileIndex = -1;
		
		public FilesGrepHandler(PrintStream resultStream) {
			this.resultStream = resultStream;
		}
		
		@Override
		public LineNumberReader nextReader() throws FileNotFoundException {
			if (++fileIndex >= files.length) {
				return null;
			}
			return new LineNumberReader(new FileReader(files[fileIndex]));
		}
		
		@Override
		public void processResult(int lineNumber, 
				String line, String match) {
			
			if (resultStream != null) {
				File file = null;
				if (files.length > 1 || withFilename) {
					file = files[fileIndex];
				}
				ResultLine resultLine = new ResultLine(
						file, lineNumber, line);
				
				resultStream.println(resultLine.toString());
			}
			
			if (results != null) {
				results.add(new GrepLineResult(files[fileIndex],
						lineNumber, line, match));
			}
		}
	}
	
	private class StreamGrepHandler implements GrepHandler {
	
		private final PrintStream resultStream;
		
		boolean done;
		
		public StreamGrepHandler(PrintStream resultStream) {
			this.resultStream = resultStream;
		}
		
		@Override
		public LineNumberReader nextReader() {
			if (done) {
				return null;
			}
			else {
				done = true;
				return new LineNumberReader(new InputStreamReader(in));
			}
		}
		
		@Override
		public void processResult(int lineNumber, 
				String line, String match) {
			
			if (resultStream != null) {
				ResultLine resultLine = new ResultLine(
						lineNumber, line);
				resultStream.println(resultLine.toString());
			}

			if (results != null) {
				results.add(new GrepLineResult(lineNumber, line, match));
			}
		}
	}

	/**
	 * Helper to build the text line that is written out for a match.
	 */
	class ResultLine {
		
		private final File file;
		private final int lineNumber;
		private final String line;
		
		public ResultLine(int lineNumber, String line) {
			this(null, lineNumber, line);
		}
		
		public ResultLine(File file, int lineNumber, String line) {
			this.file = file;
			this.lineNumber = lineNumber;
			this.line = line;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (file != null && !noFilename) {
				if (noPath) {
					builder.append(file.getName());
				}
				else {
					builder.append(file.getPath());
				}
				builder.append(':');
			}

			if (lineNumbers) {
				builder.append(lineNumber);
				builder.append(':');
			}
			
			builder.append(line);
			
			return builder.toString();
		}
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public InputStream getIn() {
		return in;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public Collection<? super GrepLineResult> getResults() {
		return results;
	}

	public void setResults(Collection<? super GrepLineResult> results) {
		this.results = results;
	}

	public int getMatchedLineCount() {
		return matchedLineCount;
	}

	public boolean isLineNumbers() {
		return lineNumbers;
	}

	public void setLineNumbers(boolean lineNumbers) {
		this.lineNumbers = lineNumbers;
	}

	public boolean isNoPath() {
		return noPath;
	}

	public void setNoPath(boolean noPath) {
		this.noPath = noPath;
	}

	public boolean isNoFilename() {
		return noFilename;
	}

	public void setNoFilename(boolean noFileName) {
		this.noFilename = noFileName;
	}

	public boolean isWithFilename() {
		return withFilename;
	}

	public void setWithFilename(boolean withFilename) {
		this.withFilename = withFilename;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}
}
