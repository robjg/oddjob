package fruit;

import java.io.Serializable;

import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;

import fruit.Colour;
import fruit.ColourType;
import fruit.ColourType.Colours;

public interface Colour extends Serializable {

	
	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {

			registry.register(Colour.class, String.class,
					new Convertlet<Colour, String>() {
				public String convert(Colour from)
						throws ConvertletException {
					
					return from.toString();
				}
			});
		}
	}
	
	public boolean isShiny();
}
