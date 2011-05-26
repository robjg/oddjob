package fruit;

import java.io.Serializable;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

public interface Flavour extends Serializable {

	
	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {

			registry.register(Flavour.class, String.class,
					new Convertlet<Flavour, String>() {
				public String convert(Flavour from)
						throws ConvertletException {
					
					return from.toString();
				}
			});
		}
	}
	
	public String getDescription();
}
