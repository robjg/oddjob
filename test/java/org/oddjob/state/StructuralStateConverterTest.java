package org.oddjob.state;

import junit.framework.TestCase;

public class StructuralStateConverterTest extends TestCase {

	public void testConvert() {
		
		ParentStateConverter test = new ParentStateConverter();
		
		assertEquals(ParentState.READY, test.toStructuralState(JobState.READY));
		assertEquals(ParentState.ACTIVE, test.toStructuralState(JobState.EXECUTING));
		assertEquals(ParentState.COMPLETE, test.toStructuralState(JobState.COMPLETE));
		assertEquals(ParentState.INCOMPLETE, test.toStructuralState(JobState.INCOMPLETE));
		assertEquals(ParentState.EXCEPTION, test.toStructuralState(JobState.EXCEPTION));
		assertEquals(ParentState.DESTROYED, test.toStructuralState(JobState.DESTROYED));
	}
}
