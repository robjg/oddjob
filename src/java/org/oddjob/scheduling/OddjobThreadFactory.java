package org.oddjob.scheduling;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.oddjob.logging.OddjobNDC;
import org.oddjob.util.Restore;

public class OddjobThreadFactory implements ThreadFactory {

	private final AtomicInteger nextThreadNum = new AtomicInteger();
	
	private final String baseName;

	public OddjobThreadFactory(String baseName) {
		Objects.requireNonNull(baseName);
		this.baseName = baseName;
	}
	
	@Override
	public Thread newThread(Runnable r) {
		
		return new Thread(new ThreadWrapper(r), 
				baseName + "-" + nextThreadNum.getAndIncrement());
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
