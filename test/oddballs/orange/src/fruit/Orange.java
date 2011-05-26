package fruit;

import java.io.Serializable;

public class Orange implements Serializable, Runnable, Fruit {
	private static final long serialVersionUID = 2010112500L;
	
	private Flavour flavour;
	
	public void run() {
		System.out.println("Orange: " + flavour);
	}
	
	public void setFlavour(Flavour colour) {
		this.flavour = colour;
	}
	
	public Flavour getFlavour() {
		return flavour;
	}
}