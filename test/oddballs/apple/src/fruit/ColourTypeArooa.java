package fruit;

import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.deploy.NoAnnotations;

public class ColourTypeArooa implements ArooaBeanDescriptor {

	@Override
	public String getComponentProperty() {
		return null;
	}

	@Override
	public ConfiguredHow getConfiguredHow(String property) {
		return ConfiguredHow.ATTRIBUTE;
	}

	@Override
	public ParsingInterceptor getParsingInterceptor() {
		return null;
	}

	@Override
	public String getTextProperty() {
		return null;
	}

	@Override
	public boolean isAuto(String property) {
		return false;
	}
	
	@Override
	public String getFlavour(String property) {
		return null;
	}
	
	@Override
	public ArooaAnnotations getAnnotations() {
		return new NoAnnotations();
	}
}
