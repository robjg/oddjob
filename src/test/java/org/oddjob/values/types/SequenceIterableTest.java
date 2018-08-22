package org.oddjob.values.types;

import org.junit.Test;

import java.util.Iterator;

import org.oddjob.OjTestCase;

public class SequenceIterableTest extends OjTestCase {

   @Test
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
	
   @Test
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
	
   @Test
	public void testAllZeroParameters() {
		
		SequenceIterable test = new SequenceIterable(0, 0, 0);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(0), it.next());
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(0), it.next());
	}
	
   @Test
	public void testFromToZeroStepOne() {
		
		SequenceIterable test = new SequenceIterable(0, 0, 1);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(0), it.next());
		
		assertFalse(it.hasNext());
	}
	
   @Test
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
	
   @Test
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
	
   @Test
	public void testLargeStep() {
		
		SequenceIterable test = new SequenceIterable(1, 10, 20);
		
		Iterator<Integer> it = test.iterator();
		
		assertTrue(it.hasNext());
		assertEquals(new Integer(1), it.next());
		
		assertFalse(it.hasNext());
	}
}
