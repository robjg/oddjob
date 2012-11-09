package org.oddjob.state;

import junit.framework.TestCase;

public class WorstStateOpTest extends TestCase {

	public void testEvaluateSingleJobOp() {
		
		WorstStateOp test = new WorstStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION));
				
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE));
				
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE));
		
	}
	
	public void testAssociative() {

		WorstStateOp test = new WorstStateOp();
		
		ParentState[] values = ParentState.values();
		
		for (int i = 0; i < values.length - 1; ++i) {
			for (int j = 0; j < values.length - 1; ++j) {
				ParentState oneWay = test.evaluate(values[i], values[j]);
				ParentState otherWay = test.evaluate(values[j], values[i]);
				
				assertSame("Failed on i=" + i + ", j = " + j,
						oneWay, otherWay);
			}
		}
	}
	
	public void testEvaluateSingleServiceOp() {
		
		WorstStateOp test = new WorstStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION));
				
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED));
				
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.COMPLETE));
		
	}
	
	public void testEvaluateTwoJobOps() {
		
		WorstStateOp test = new WorstStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.READY, JobState.EXECUTING));
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.READY, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.READY, JobState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.EXECUTING));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.EXECUTING, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXECUTING, JobState.EXCEPTION));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.READY));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.EXECUTING));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.INCOMPLETE, JobState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.COMPLETE, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.COMPLETE, JobState.EXECUTING));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.COMPLETE, JobState.EXCEPTION));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.READY));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.EXECUTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.EXCEPTION));
	}
	
	public void testEvaluateTwoServiceOps() {
		
		WorstStateOp test = new WorstStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY, ServiceState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.READY, ServiceState.STARTING));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY, ServiceState.COMPLETE));

		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.READY, ServiceState.STARTED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.READY, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.STARTING));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.COMPLETE));

		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.STARTED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STARTING, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, ServiceState.READY));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, ServiceState.STARTED));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, ServiceState.COMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STARTED, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.STARTING));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.COMPLETE));

		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.STARTED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.READY));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.STARTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.STARTED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.EXCEPTION));
	}
	
	public void testEvaluateAJobAndAServiceOp() {
		
		WorstStateOp test = new WorstStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.READY, JobState.EXECUTING));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(ServiceState.READY, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.READY, JobState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, JobState.EXECUTING));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(ServiceState.STARTING, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STARTING, JobState.EXCEPTION));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTED, JobState.EXECUTING));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(ServiceState.STARTED, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STARTED, JobState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.COMPLETE, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.COMPLETE, JobState.EXECUTING));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.COMPLETE, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(ServiceState.COMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.COMPLETE, JobState.EXCEPTION));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.READY));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.EXECUTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.EXCEPTION));
	}
	
	public void testDestroyed() {

		
		WorstStateOp test = new WorstStateOp();
		
		try {
			assertEquals(JobState.DESTROYED, 
					test.evaluate(JobState.DESTROYED, JobState.DESTROYED));
			fail("Should fail");
		} catch (IllegalStateException e) {
			// expected.
		}
	}
}
