package org.oddjob.scheduling;

import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MockScheduledFuture<T> extends MockFuture<T>
implements ScheduledFuture<T> {

	public long getDelay(TimeUnit unit) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public int compareTo(Delayed o) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
