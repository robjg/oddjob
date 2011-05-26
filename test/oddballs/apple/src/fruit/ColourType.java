package fruit;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;

public class ColourType implements ArooaValue {

	enum Colours {
		RED, GREEN
	}
	
	public static class Conversions implements ConversionProvider {
	
		public void registerWith(ConversionRegistry registry) {

			registry.register(String.class, Colours.class,
					new Convertlet<String, Colours>() {
				public Colours convert(String from)
						throws ConvertletException {
					
					return Colours.valueOf(from);
				}
			});
			registry.register(ColourType.class, Colour.class, 
					new Convertlet<ColourType, Colour>() {
				
				public Colour convert(ColourType from)
						throws ConvertletException {
					switch (from.colour) {
					case GREEN:
						return new Colour() {
							public boolean isShiny() {
								return false;
							}
							@Override
							public String toString() {
								return "Green";
							}
						};
					case RED:
						return new Colour() {
							public boolean isShiny() {
								return true;
							}
							@Override
							public String toString() {
								return "Red";
							}
						};
					default:
						throw new IllegalStateException("Unrecognized Colour");
					}
				}
			});
		}
	}

	private boolean shiny;
	
	private Colours colour;
	
	public Colours getColour() {
		return colour;
	}

	public void setColour(Colours colour) {
		this.colour = colour;
	}

	public boolean isShiny() {
		return shiny;
	}

	public void setShiny(boolean shiny) {
		this.shiny = shiny;
	}

}