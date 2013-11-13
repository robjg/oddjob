package org.oddjob.values.types;

import java.util.Iterator;

import junit.framework.TestCase;

public class SequenceIterableTest extends TestCase {

	public void testSimplePositiveSingleStep() {
		
		SequenceIterable test = new SequenceIterable(1, 3, 1);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(1), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(2), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(3), it.next());
		
		assertFalse(it.hasNext());
		
	}
	
	public void testSimpleNegativeSingleStep() {
		
		SequenceIterable test = new SequenceIterable(-1, -3, -1);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(-1), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(-2), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(-3), it.next());
		
		assertFalse(it.hasNext());
	}
	
	public void testAllZeroParameters() {
		
		SequenceIterable test = new SequenceIterable(0, 0, 0);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(0), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(0), it.next());
	}
	
	public void testFromToZeroStepOne() {
		
		SequenceIterable test = new SequenceIterable(0, 0, 1);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(0), it.next());
		
		assertFalse(it.hasNext());
	}
	
	public void testToLessThanFromPositiveStep() {
		
		SequenceIterable test = new SequenceIterable(0, -1, 1);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(0), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(1), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(2), it.next());
				
		assertTrue(it.hasNext());
		assertEquals(new Integer(3), it.next());
	}
	
	public void testBigSteps() {
		
		SequenceIterable test = new SequenceIterable(0, 30, 10);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(0), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(10), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(20), it.next());
				
		assertTrue(it.hasNext());
		assertEquals(new Integer(30), it.next());
	}
	
	public void testLargeStep() {
		
		SequenceIterable test = new SequenceIterable(1, 10, 20);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(1), it.next());
		
		assertFalse(it.hasNext());
	}
}
