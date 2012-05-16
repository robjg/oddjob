package org.oddjob.sql;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.beanbus.BeanTrap;
import org.oddjob.beanbus.BusException;
import org.oddjob.io.BufferType;
import org.oddjob.sql.SQLJob.DelimiterType;

public class SQLScriptParserTest extends TestCase {

	public void testNoDelimiter() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		
		BufferType buffer = new BufferType();
		buffer.setText("SIMPLE TEXT");
		buffer.configured();
		
		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(1, stmts.size());
		assertEquals("SIMPLE TEXT", stmts.get(0));
	}
	
	public void testOneEmptyLine() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		
		BufferType buffer = new BufferType();
		buffer.setText("LINE ONE\n\nLINE TWO\n");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(2, stmts.size());
		assertEquals("LINE ONE", stmts.get(0));
		assertEquals("LINE TWO", stmts.get(1));
	}
	
	public void testLotsOfEmptyLines() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		
		BufferType buffer = new BufferType();
		buffer.setText("\n\n\nLINE ONE\n\n\n\nLINE TWO\n\n\n");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(2, stmts.size());
		assertEquals("LINE ONE", stmts.get(0));
		assertEquals("LINE TWO", stmts.get(1));
	}
	
	public void testWindowsLines() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		
		BufferType buffer = new BufferType();
		buffer.setText("\r\n\r\n\r\nLINE ONE\r\n\r\n\r\n\r\nLINE TWO\r\n\r\n\r\n");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(2, stmts.size());
		assertEquals("LINE ONE", stmts.get(0));
		assertEquals("LINE TWO", stmts.get(1));
	}
	
	public void testComments() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		
		BufferType buffer = new BufferType();
		buffer.setText("--LINE ONE\n\nLINE TWO\n");
		buffer.configured();
		
		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(1, stmts.size());
		assertEquals("LINE TWO", stmts.get(0));
	}
	
	public void testDefaultDelimited() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		
		BufferType buffer = new BufferType();
		buffer.setText("\nONE;\nTWO;\nTHREE\n");
		buffer.configured();
		
		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(3, stmts.size());
		assertEquals("ONE", stmts.get(0));
		assertEquals("TWO", stmts.get(1));
		assertEquals("THREE", stmts.get(2));
	}
	
	public void testNonRowDelimiterOnSeperateLine() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		test.setDelimiter("@");
		
		BufferType buffer = new BufferType();
		buffer.setText("\nONE\n@\n" +
				"\nTWO@\n" +
				"THREE\n@\n\n\n\n");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(3, stmts.size());
		assertEquals("ONE", stmts.get(0));
		assertEquals("TWO", stmts.get(1));
		assertEquals("THREE", stmts.get(2));
	}
	
	public void testGoDelimiter() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		test.setDelimiter("GO");
		test.setDelimiterType(DelimiterType.ROW);
		
		BufferType buffer = new BufferType();
		buffer.setText("\nONE\ngo\nTWO\nGO\nTHREE\nGO");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(3, stmts.size());
		assertEquals("ONE", stmts.get(0));
		assertEquals("TWO", stmts.get(1));
		assertEquals("THREE", stmts.get(2));
	}
	
	public void testGoDelimiterWithBlankLines() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		test.setDelimiter("GO");
		test.setDelimiterType(DelimiterType.ROW);
		
		BufferType buffer = new BufferType();
		buffer.setText("\nONE\n\ngo\nTWO\nGO\n\nTHREE\n\n");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(3, stmts.size());
		assertEquals("ONE", stmts.get(0));
		assertEquals("TWO", stmts.get(1));
		assertEquals("THREE", stmts.get(2));
	}
	
	public void testGoDelimiterWithBlankLines2() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		test.setDelimiter("GO");
		test.setDelimiterType(DelimiterType.ROW);
		
		BufferType buffer = new BufferType();
		buffer.setText("\nONE\n\ngo\n\nTWO\n\nGO\n\nTHREE\nGO\n\n");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals("ONE", stmts.get(0));
		assertEquals("TWO", stmts.get(1));
		assertEquals("THREE", stmts.get(2));
		assertEquals(3, stmts.size());
	}
	
	public void testGoDelimiterWithMultipleLines() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		test.setDelimiter("GO");
		test.setDelimiterType(DelimiterType.ROW);
		
		BufferType buffer = new BufferType();
		buffer.setText(
				"\n" +
				"ONE\n" +
				"BANANA\n" +
				"go\n" +
				"\n" +
				"TWO\n" +
				"\n" +
				"BANANAS\n" +
				"\n" +
				"GO\n" +
				"\n" +
				"THREE\n" +
				"BANANAS\n" +
				"FOUR\n" +
				"GO\n" +
				"\n");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals("ONE BANANA", stmts.get(0));
		assertEquals("TWO  BANANAS", stmts.get(1));
		assertEquals("THREE BANANAS FOUR", stmts.get(2));
		assertEquals(3, stmts.size());
	}
	
	public void testReplaceProperties() throws IOException, BusException {
		
		BeanTrap<String> results = new BeanTrap<String>();
		
		ScriptParser test = new ScriptParser();
		test.setExpandProperties(true);
		
		StandardArooaSession session = new StandardArooaSession();
		session.getBeanRegistry().register("stuff", "OK");
		
		test.setArooaSession(session);
		
		BufferType buffer = new BufferType();
		buffer.setText("THIS IS ${stuff}");
		buffer.configured();

		test.setInput(buffer.toInputStream());
		test.setTo(results);
		
		test.go();
		
		List<String> stmts = results.toValue();
		
		assertEquals(1, stmts.size());
		assertEquals("THIS IS OK", stmts.get(0));
	}
}
