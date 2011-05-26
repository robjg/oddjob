package fruit;

import java.io.Serializable;

public class Apple implements Serializable, Runnable, Fruit {
	
	private Colour colour;
	
	public void run() {
		System.out.println("Apple: " + colour);
	}
	
	public void setColour(Colour colour) {
		this.colour = colour;
	}
	
	public Colour getColour() {
		return colour;
	}
}