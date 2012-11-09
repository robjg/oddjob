package org.oddjob.state;

import junit.framework.TestCase;

public class ServiceManagerStateOpTest extends TestCase {

	public void testEvaluateSingleJobOp() {
		
		ServiceManagerStateOp test = new ServiceManagerStateOp();
		
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
	
	public void testEvaluateSingleServiceOp() {
		
		ServiceManagerStateOp test = new ServiceManagerStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.STARTED));
				
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION));
				
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.COMPLETE));
	}
	
	public void testAssociative() {

		ServiceManagerStateOp test = new ServiceManagerStateOp();
		
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

	
	public void testEvaluateTwoOps() {
		
		ServiceManagerStateOp test = new ServiceManagerStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.READY));
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.EXECUTING));
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.READY, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.READY, JobState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
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
	
	public void testEvaluateTwoOpsService() {
		
		ServiceManagerStateOp test = new ServiceManagerStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY, ServiceState.READY));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY, ServiceState.STARTED));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.READY, ServiceState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.READY, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.STARTING, ServiceState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.STARTED));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.COMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STARTED, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.READY));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.STARTED));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.COMPLETE, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.READY));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.STARTED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.EXCEPTION));
	}
	
	public void testDestroyed() {
		
		ServiceManagerStateOp test = new ServiceManagerStateOp();
		
		try {
			assertEquals(JobState.DESTROYED, 
					test.evaluate(JobState.DESTROYED, JobState.DESTROYED));
			fail("Should fail");
		} catch (IllegalStateException e) {
			// expected.
		}
	}
}
