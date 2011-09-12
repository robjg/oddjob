package org.oddjob.values.types;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.ValueFactory;

public class IdentifiableValueType 
implements ValueFactory<ArooaValue>, ArooaSessionAware, ArooaLifeAware {

	private String idx;
	
	private ArooaValue value;
	
	private ArooaSession session;
	
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public ArooaValue toValue() throws ArooaConversionException {
		return value;
	}

	public String getIdx() {
		return idx;
	}

	public void setIdx(String id) {
		this.idx = id;
	}

	public ArooaValue getValue() {
		return value;
	}

	public void setValue(ArooaValue value) {
		this.value = value;
	}
	
	@Override
	public void initialised() {
	}
	
	@Override
	public void configured() {
		if (idx == null) {
			throw new IllegalStateException("No Id provided.");
		}
		
		if (value != null) {
			session.getBeanRegistry().register(idx, value);
		}
	}
	
	@Override
	public void destroy() {
		if (value != null) {
			session.getBeanRegistry().remove(value);
		}
	}	
}
