package org.oddjob.scheduling;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.oddjob.logging.OddjobNDC;
import org.oddjob.util.Restore;

public class OddjobThreadFactory implements ThreadFactory {

	private static final ConcurrentMap<String, AtomicInteger> poolNames = new ConcurrentHashMap<>();
	
	private final AtomicInteger nextThreadNum = new AtomicInteger();
	
	private final String poolName;

	public OddjobThreadFactory(String baseName) {
		baseName = Optional.ofNullable(baseName).orElse("Oddjob");
		AtomicInteger count = poolNames.computeIfAbsent(baseName, s -> new AtomicInteger());
		this.poolName = baseName + "-" + count.getAndIncrement();
	}
	
	@Override
	public Thread newThread(Runnable r) {
		
		return new Thread(new ThreadWrapper(r), 
				poolName + "-" + nextThreadNum.getAndIncrement());
	}
	
	static class ThreadWrapper implements Runnable {
		
		private final Runnable wrapped;
		
		ThreadWrapper(Runnable wrapped) {
			this.wrapped = wrapped;
		}
		
		@Override
		public void run() {
			
			Optional<OddjobNDC.LogContext> logContext = OddjobNDC.current();
			try (Restore restore = logContext
					.map(lc -> OddjobNDC.setLoggingNDC(lc))
					.orElse(() -> {})) {
				wrapped.run();
			}
		}
	}
	
}
