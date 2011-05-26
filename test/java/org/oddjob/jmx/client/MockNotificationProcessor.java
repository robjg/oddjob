package org.oddjob.jmx.client;

public class MockNotificationProcessor implements NotificationProcessor {

	public void enqueueDelayed(Runnable runnable, long delay) {
		throw new RuntimeException("Unexpected.");
	}

	public void enqueue(Runnable o) {
		throw new RuntimeException("Unexpected.");
	}

	public void run() {
		throw new RuntimeException("Unexpected.");
	}

	public int size() {
		throw new RuntimeException("Unexpected.");
	}
	
}
