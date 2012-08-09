package org.oddjob.jmx.general;

import java.util.Date;

public class Vendor implements VendorMBean {
	
	String fruit;
	Date delivery;
	int quantity;
	
	double rating;
	
	final String farm;
	
	public Vendor(String farm) {
		this.farm = farm;
	}
	
	public double quote(String fruit, Date delivery, int quantity) {
		
		this.fruit = fruit;
		this.delivery= delivery;
		this.quantity = quantity;
		
		return 94.23;
	}
	
	@Override
	public String getFarm() {
		return farm;
	}
	
	@Override
	public void setRating(double rating) {
		this.rating = rating;
	}
	
	@Override
	public double getRating() {
		return rating;
	}
}