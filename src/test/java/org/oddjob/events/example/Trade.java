package org.oddjob.events.example;

public class Trade {

    private String product;

	private int quantity;
    
    private double price;

    public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
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
