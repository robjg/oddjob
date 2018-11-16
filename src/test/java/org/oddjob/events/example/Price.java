package org.oddjob.events.example;

public class Price {

    private String product;
    private double price;

    public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
    
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + product;
	}

}
