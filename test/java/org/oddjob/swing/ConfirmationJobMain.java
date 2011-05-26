package org.oddjob.swing;

public class ConfirmationJobMain {

	public static void main(String... args) {
		
		ConfirmationJob test = new ConfirmationJob();
		
		test.setTitle("Test");
		test.setMessage("Continue?");
		
		test.run();
		
		System.out.println(test.lastJobStateEvent().getJobState());
	}
}
