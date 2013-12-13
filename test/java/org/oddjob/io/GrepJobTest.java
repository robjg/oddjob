package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OurDirs;

public class GrepJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(GrepJobTest.class);
	
	File dir;

	public void setUp() throws Exception {
		OurDirs dirs = new OurDirs();
		dir = new File(dirs.base(), "test/io");
	}
		
	public void testFromInput() throws IOException {
		
		BufferType buffer = new BufferType();
		buffer.setLines(new String[] {
				"Five Blue Trains", 
				"Two Green Lorries", 
				"Seven Red Cars"});
		buffer.configured();
		
		BufferType results = new BufferType();
		results.configured();
		
		List<GrepLineResult> resultBeans = new ArrayList<GrepLineResult>();
		
		GrepJob test = new GrepJob();
		test.setText("Red");
		test.setIn(buffer.toInputStream());
		test.setOut(results.toOutputStream());
		test.setResults(resultBeans);
		test.setLineNumbers(false);
		test.call();
		
		assertEquals(1, test.getMatchedLineCount());
		
		String[] resultLines = results.getLines();
		
		assertEquals("Seven Red Cars", resultLines[0]);
		
		GrepLineResult resultBean = resultBeans.get(0);
		assertEquals(null, resultBean.getFile());
		assertEquals(3, resultBean.getLineNumber());
		assertEquals("Seven Red Cars", resultBean.getLine());
				
		// With line number
		
		results.configured();
		
		test.setLineNumbers(true);
		test.setIn(buffer.toInputStream());
		test.setOut(results.toOutputStream());
		test.call();
		
		resultLines = results.getLines();
		
		assertEquals("3:Seven Red Cars", resultLines[0]);
		
	}
	
	public void testGrepSimpleTextFile() throws IOException {
		
		BufferType results = new BufferType();
		results.configured();
		
		List<GrepLineResult> resultBeans = new ArrayList<GrepLineResult>();
		
		File testFile = new File(dir, "GrepTest1.txt");
		
		GrepJob test = new GrepJob();
		test.setText("oranges");
		test.setFiles(new File[] { testFile } );
		test.setOut(results.toOutputStream());
		test.setResults(resultBeans);
		
		test.call();
		
		assertEquals(1, test.getMatchedLineCount());
		
		String[] resultLines = results.getLines();
		
		assertEquals("All oranges are orange.", 
				resultLines[0]);
		
		GrepLineResult resultBean = resultBeans.get(0);
		assertEquals(testFile, resultBean.getFile());
		assertEquals(2, resultBean.getLineNumber());
		assertEquals("All oranges are orange.", resultBean.getLine());
		
		// With file.
		
		results.configured();
		
		test.setWithFilename(true);
		test.setOut(results.toOutputStream());
		test.setResults(null);
		test.call();
		
		resultLines = results.getLines();
		
		assertEquals(testFile.getPath() + ":All oranges are orange.", 
				resultLines[0]);
	}
	
	public void testGrepRegexp() throws IOException {
		
		BufferType results = new BufferType();
		results.configured();
		
		List<GrepLineResult> resultBeans = new ArrayList<GrepLineResult>();
		
		File testFile = new File(dir, "GrepTest1.txt");
		
		GrepJob test = new GrepJob();
		// find only pears in quotes
		test.setText("\"[^\"]*\\bpears\\b[^\"]*\"");
		test.setRegexp(true);
		test.setFiles(new File[] { testFile } );
		test.setOut(results.toOutputStream());
		test.setResults(resultBeans);
		
		test.call();
		
		assertEquals(1, test.getMatchedLineCount());
		
		String[] resultLines = results.getLines();
		
		assertEquals("Most \"pears are green\".", 
				resultLines[0]);
		
		GrepLineResult resultBean = resultBeans.get(0);
		assertEquals(testFile, resultBean.getFile());
		assertEquals(4, resultBean.getLineNumber());
		assertEquals("Most \"pears are green\".", resultBean.getLine());
		assertEquals("\"pears are green\"", resultBean.getMatch());
	}
	
	public void testMultipleFiles() throws IOException {
				
		BufferType results = new BufferType();
		results.configured();
				
		File testFile1 = new File(dir, "GrepTest1.txt");
		File testFile2 = new File(dir, "GrepTest2.txt");
		
		GrepJob test = new GrepJob();
		
		test.setText("red");
		test.setFiles(new File[] { testFile1, testFile2 } );
		test.setOut(results.toOutputStream());
		
		test.call(); 
		
		assertEquals(3, test.getMatchedLineCount());
		
		System.out.println(results.getText());
		
		String[] resultLines = results.getLines();
		
		assertEquals(testFile1.getPath() + ":Some apples are red.", 
				resultLines[0]);
		assertEquals(testFile1.getPath() + ":Few pears are red.", 
				resultLines[1]);
		assertEquals(testFile2.getPath() + ":2 red buses.", 
				resultLines[2]);
		
		// Without path with line numbers.
		
		results.configured();
		
		test.setNoPath(true);
		test.setLineNumbers(true);
		test.setOut(results.toOutputStream());
		test.setResults(null);
		test.call();
		
		assertEquals(3, test.getMatchedLineCount());
		
		resultLines = results.getLines();
		
		assertEquals("GrepTest1.txt:1:Some apples are red.", 
				resultLines[0]);
		assertEquals("GrepTest1.txt:3:Few pears are red.", 
				resultLines[1]);
		assertEquals("GrepTest2.txt:2:2 red buses.", 
				resultLines[2]);
		
		// Without file.
		
		results.configured();
		
		test.setNoFilename(true);
		test.setLineNumbers(false);
		test.setOut(results.toOutputStream());
		test.setResults(null);
		test.call();
		
		assertEquals(3, test.getMatchedLineCount());
		
		resultLines = results.getLines();
		
		assertEquals("Some apples are red.", 
				resultLines[0]);
		assertEquals("Few pears are red.", 
				resultLines[1]);
		assertEquals("2 red buses.", 
				resultLines[2]);
		
	}
	
	public void testInvertAndCase() throws IOException {
		
		BufferType results = new BufferType();
		results.configured();
				
		File testFile2 = new File(dir, "GrepTest2.txt");
		
		GrepJob test = new GrepJob();
		
		test.setText("red");
		test.setFiles(new File[] { testFile2 } );
		test.setOut(results.toOutputStream());
		test.setInvert(true);
		
		test.call(); 
		
		assertEquals(2, test.getMatchedLineCount());
		
		System.out.println(results.getText());
		
		String[] resultLines = results.getLines();
		
		assertEquals("5 green cars.", 
				resultLines[0]);
		assertEquals("1 RED lorry.", 
				resultLines[1]);
		
		// Ignore case.
		
		results.configured();
		
		test.setIgnoreCase(true);
		test.setOut(results.toOutputStream());
		test.setResults(null);
		test.call();
		
		assertEquals(1, test.getMatchedLineCount());
		
		resultLines = results.getLines();
		
		assertEquals("5 green cars.", 
				resultLines[0]);
		
		// No invert.
		
		results.configured();
		
		test.setInvert(false);
		test.setOut(results.toOutputStream());
		test.setResults(null);
		test.call();
		
		assertEquals(2, test.getMatchedLineCount());
		
		resultLines = results.getLines();
		
		assertEquals("2 red buses.", 
				resultLines[0]);
		assertEquals("1 RED lorry.", 
				resultLines[1]);
		
	}
	
	public void testGrepJobExample() {
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/GrepJobExample.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();

		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("2:2 red buses.", lines[0].trim());
		assertEquals("3:1 RED lorry.", lines[1].trim());
		
		assertEquals(2, lines.length);
		
		oddjob.destroy();
	}
}