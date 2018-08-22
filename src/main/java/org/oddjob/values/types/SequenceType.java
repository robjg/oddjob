package org.oddjob.values.types;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description A sequence.
 * 
 * @author rob
 *
 */
public class SequenceType implements ValueFactory<Iterable<Integer>>{

	private Integer from;
	
	private Integer to;
	
	private Integer step;
		
	@Override
	public Iterable<Integer> toValue() throws ArooaConversionException {
		
		return new SequenceIterable(
				from == null ? 0 : from, 
				to == null ? 0 : to,
				step == null ? 1 : step);
	}

	public Integer getFrom() {
		return from;
	}

	public void setFrom(Integer from) {
		this.from = from;
	}

	public Integer getTo() {
		return to;
	}

	public void setTo(Integer to) {
		this.to = to;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + 
				(from == null ? "" : ", from " + from) + 
				(to == null ? "" : ", to " + to) + 
				(step == null ? "" : ", step " + step);
	}
}
