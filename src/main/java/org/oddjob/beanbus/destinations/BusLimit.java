package org.oddjob.beanbus.destinations;

import org.oddjob.FailedToStopException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.framework.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @oddjob.description Only allow a certain number of beans passed. When the limit is reached the Bus will
 * be Stopped. Any beans arriving while the bus is stopping will be ignored.
 *
 * @oddjob.example Limit the Bus to 2 beans.
 *
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BusLimitExample.xml}
 *
 * @author rob
 *
 * @param <F>
 */
public class BusLimit<F> implements Service, BusFilter<F, F> {

	private static final Logger logger = LoggerFactory.getLogger(BusLimit.class);

	/**
	 * @oddjob.property
	 * @oddjob.description The next component in a bus. Set automatically in a
	 * {@link org.oddjob.beanbus.bus.BasicBusService}.
	 * @oddjob.required No.
	 */
	private volatile Consumer<? super F> to;

	/**
	 * @oddjob.property
	 * @oddjob.description The name of this component.
	 * @oddjob.required No.
	 */
	private volatile String name;

	/**
	 * @oddjob.property
	 * @oddjob.description The limit.
	 * @oddjob.required No, defaults to 0.
	 */
	private int limit;

	/**
	 * @oddjob.property
	 * @oddjob.description The number so far.
	 * @oddjob.required Read only.
	 */
	private final AtomicInteger count = new AtomicInteger();
	
	private BusConductor busConductor;

	@Override
	public void stop() throws FailedToStopException {

	}

	@Override
	public void start() throws Exception {
		count.set(0);

		Objects.requireNonNull(this.busConductor, "No Bus Conductor");

		if (limit < 1) {
			throw new IllegalArgumentException("limit must be greater than 0.");
		}
	}

	@Override
	public void accept(F from) {

		int now = count.incrementAndGet();

		if (now > limit) {
			logger.info("Ignoring {} as it is passed the limit.", from);
			return;
		}

		if (now == limit) {

			if (to != null) {
				to.accept(from);
			}

			busConductor.close();
		}
		else {

			if (to != null) {
				to.accept(from);
			}
		}
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getCount() {
		return count.get();
	}
	
	@ArooaHidden
	@Inject
	public void setBusConductor(BusConductor busConductor) {

		this.busConductor = busConductor;
	}

	@Override
	public void setTo(Consumer<? super F> to) {
		this.to = to;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {

		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}
}
