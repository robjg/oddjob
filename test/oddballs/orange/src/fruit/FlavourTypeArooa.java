package fruit;

import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.ParsingInterceptor;

public class FlavourTypeArooa implements ArooaBeanDescriptor {

	public String getComponentProperty() {
		return null;
	}

	public ConfiguredHow getConfiguredHow(String property) {
		return ConfiguredHow.ATTRIBUTE;
	}

	public ParsingInterceptor getParsingInterceptor() {
		return null;
	}

	public String getTextProperty() {
		return null;
	}

	public boolean isAuto(String property) {
		return false;
	}
	
	public String getFlavour(String property) {
		return null;
	}
}
