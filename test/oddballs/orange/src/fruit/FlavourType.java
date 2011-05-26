package fruit;


public class FlavourType implements Flavour {
	private static final long serialVersionUID = 2010112500L;
	private String description;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return "Flaovur is " + description;
	}
}