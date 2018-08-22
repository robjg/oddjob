package org.oddjob.examples;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;

public class PricingJob implements Runnable {

	private Object priceService;
	
	public Object getPriceService() {
		return priceService;
	}

	@ArooaAttribute
	public void setPriceService(Object priceService) {
		this.priceService = priceService;
	}

	@Override
	public void run() {
		if (priceService == null) {
			throw new NullPointerException("No Price Service");
		}
	}	
}
