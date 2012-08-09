package org.oddjob.jmx.general;

import java.util.Date;

public interface VendorMBean {
	
	public double quote(String fruit, Date delivery, int quantity);
	
	public String getFarm();
	
	public void setRating(double rating);
	
	public double getRating();
}